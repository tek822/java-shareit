package ru.practicum.shareit.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingSimpleDto;
import ru.practicum.shareit.booking.exception.BookingBadRequestException;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.practicum.shareit.booking.dto.BookingValidator.isValid;
import static ru.practicum.shareit.util.Util.*;

@Slf4j
@Service
public class BookingServiceImpl implements BookingService {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private BookingRepository bookingRepository;

    @Override
    public BookingDto add(long userId, BookingSimpleDto bookingSimpleDto) {
        User booker = getUser(userRepository, userId);
        Item item = getItem(itemRepository, bookingSimpleDto.getItemId());
        if (!isValid(bookingSimpleDto)) {
            throw new BookingBadRequestException("Ошибка валидации резервирования: " + bookingSimpleDto);
        }
        if (!item.getAvailable()) {
            throw new BookingBadRequestException("Бронирование недоступно: " + bookingSimpleDto);
        }
        if (item.getOwner().getId() == booker.getId()) {
            throw new BookingNotFoundException(
                    String.format("Владелец с id:%d, не может забронировать предмет: %s", booker.getId(), item.getId())
            );
        }
        Booking booking = modelMapper.map(bookingSimpleDto, Booking.class);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);
        log.info("Создано резервирование: {}", booking);
        return modelMapper.map(bookingRepository.save(booking), BookingDto.class);
    }

    @Override
    public BookingDto approve(long bookingId, long userId, boolean approve) {
        User user = getUser(userRepository, userId);
        Booking booking = getBooking(bookingRepository, bookingId);

        if (user.getId() != booking.getItem().getOwner().getId()) {
            throw new BookingNotFoundException("Пользователь не может изменить резервирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BookingBadRequestException(
                    String.format("Можно измениться только %s статус резервирования", BookingStatus.WAITING)
            );
        }

        booking.setStatus(approve ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        log.info("Изменен статус резервировния: {}", booking);
        return modelMapper.map(bookingRepository.save(booking), BookingDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto get(long bookingId, long userId) {
        Booking booking = getBooking(bookingRepository, bookingId);
        User user = getUser(userRepository, userId);

        if (userId == booking.getBooker().getId() || userId == booking.getItem().getOwner().getId()) {
            log.info("Запрошено резервирование с id: {}, для пользовтаеля с id: {}", bookingId, userId);
            return modelMapper.map(booking, BookingDto.class);
        } else {
            log.info("Запрещен просмотр резервирования с id: {}, для пользовтаеля с id: {}", bookingId, userId);
            throw new BookingNotFoundException("Просмотр резервирования запрещен");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getOwnBookings(long userId, String state) {
        BookingState bookingState = getState(state);
        User user = getUser(userRepository, userId);
        log.info("Запрошены все собственные резервирования со статусом '{}' пользователя с id: {}", state, userId);
        List<Booking> bookings = bookingRepository.findAllByBooker(userId);
        if (bookings != null) {
            bookings = filterByState(bookingState, bookings);
            return bookings.stream()
                    .map(booking -> modelMapper.map(booking, BookingDto.class))
                    .collect(Collectors.toList());
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsForOwnItems(long userId, String state) {
        BookingState bookingState = getState(state);
        User user = getUser(userRepository, userId);
        log.info("Запрошены все резервирования со статусом '{}' для предметов пользователя с id: {}", state, userId);
        List<Booking> bookings = bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId);
        if (bookings != null) {
            bookings = filterByState(bookingState, bookings);
            return bookings.stream()
                    .map(booking -> modelMapper.map(booking, BookingDto.class))
                    .collect(Collectors.toList());
        }
        return null;
    }

    private List<Booking> filterByState(BookingState state, List<Booking> bookings) {
        LocalDateTime now = LocalDateTime.now();
        Stream<Booking> stream = bookings.stream();
        switch (state) {
            case CURRENT:
                stream = stream.filter(booking ->
                        (booking.getStart().isBefore(now) && booking.getEnd().isAfter(now))
                );
                break;
            case FUTURE:
                stream = stream.filter(booking -> booking.getStart().isAfter(now));
                break;
            case WAITING:
                stream = stream.filter(booking ->
                        (booking.getStart().isAfter(now)
                                && booking.getStatus() == BookingStatus.WAITING));
                break;
            case REJECTED:
                stream = stream.filter(booking ->
                        booking.getStatus() == BookingStatus.CANCELED
                                || booking.getStatus() == BookingStatus.REJECTED);
                break;
            case PAST:
                stream = stream.filter(booking -> booking.getEnd().isBefore(now));
                break;
            default:
                break;
        }
        return stream.collect(Collectors.toList());
    }

    private BookingState getState(String state) {
        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new BookingBadRequestException("Unknown state: " + state);
        }
        return  bookingState;
    }
}
