package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingSimpleDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.CommentBadRequestException;
import ru.practicum.shareit.item.exception.UpdateForbiddenException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.util.Util.getItem;
import static ru.practicum.shareit.util.Util.getUser;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private CommentRepository commentRepository;

    @Override
    public ItemDto add(long userId, ItemDto itemDto) {
        User user = getUser(userRepository, userId);
        Item item = modelMapper.map(itemDto, Item.class);
        item.setOwner(user);
        long itemId = itemRepository.save(item).getId();
        log.info("uid: {}, добавил item с id: {}", userId, itemId);
        itemDto.setId(itemId);
        return itemDto;
    }

    @Override
    public ItemDto update(long userId, ItemDto itemDto) {
        User user = getUser(userRepository, userId);
        Item item = getItem(itemRepository, itemDto.getId());
        if (userId != item.getOwner().getId()) {
            throw new UpdateForbiddenException("Пользователь с id " + user.getId()
                    + " не является владельцем предмета с id " + itemDto.getId());
        }
        Item update = modelMapper.map(itemDto, Item.class);
        modelMapper.map(update, item);
        log.info("uid: {}, обновил item с id: {}", userId, itemDto.getId());
        return modelMapper.map(itemRepository.save(item), ItemDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto get(long itemId, long userId) {
        Item item = getItem(itemRepository, itemId);
        ItemDto itemDto = modelMapper.map(item, ItemDto.class);
        if (item.getOwner().getId() == userId) {
            getBookingForItem(itemDto);
        }
        getCommentsForItem(itemDto);
        log.info("Пользователем с id: {} запрошен предмет с id: {}", userId, itemId);
        return itemDto;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ItemDto> getAll(long userId) {
        User user = getUser(userRepository, userId);
        log.info("Запрошены предметы пользователя с id: {}", userId);
        List<ItemDto> items = itemRepository.findAllByUserId(userId).stream()
                .map(i -> modelMapper.map(i, ItemDto.class))
                .collect(Collectors.toList());
        for (ItemDto item : items) {
            getBookingForItem(item);
            getCommentsForItem(item);
        }
        return items;
    }

    @Transactional(readOnly = true)
    private void getBookingForItem(ItemDto item) {
        List<Booking> itemBookings = bookingRepository.findAllByItemId(item.getId());
        LocalDateTime now = LocalDateTime.now();
        for (Booking booking : itemBookings) {
            if (booking.getStart().isBefore(now)) {
                item.setLastBooking(modelMapper.map(booking, BookingSimpleDto.class));
            } else {
                item.setNextBooking(modelMapper.map(booking, BookingSimpleDto.class));
                break;
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ItemDto> findAvailable(String text) {
        log.info("Поиск предметов со строкой: {}", text);
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        String lowerCase = text.toLowerCase();
        return itemRepository.findAllByDescriptionContainingIgnoreCaseOrNameContainingIgnoreCase(lowerCase, lowerCase).stream()
                .filter(i -> ((i.getAvailable() != null) && i.getAvailable()))
                .map(i -> modelMapper.map(i, ItemDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(long itemId, long userId, CommentDto commentDto) {
        Item item = getItem(itemRepository, itemId);
        User user = getUser(userRepository, userId);
        LocalDateTime now = LocalDateTime.now();
        if (bookingRepository.findAllByBooker(userId).stream()
                        .filter(booking -> booking.getEnd().isBefore(now))
                        .filter(booking -> booking.getStatus() == BookingStatus.APPROVED)
                        .collect(Collectors.toList())
                        .isEmpty()) {
            log.info("Пользователь с id:{} не может комментировать item с id:{}", userId, itemId);
            throw new CommentBadRequestException(
                    String.format("Пользователь с id:%d не может комментировать item с id:%d", userId, itemId));
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setAuthor(user);
        comment.setItem(item);
        comment.setCreated(now);

        commentRepository.save(comment);
        log.info("Пользователь с id:{} создал комментарий к item с id:{}", userId, itemId);
        return modelMapper.map(comment, CommentDto.class);
    }

    @Transactional(readOnly = true)
    private void getCommentsForItem(ItemDto itemDto) {
        List<Comment> comments = commentRepository.findAllByItemId(itemDto.getId());
        itemDto.setComments(
            comments.stream()
                .map(comment -> modelMapper.map(comment, CommentDto.class))
                .collect(Collectors.toList())
        );
    }
}
