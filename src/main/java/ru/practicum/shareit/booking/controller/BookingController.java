package ru.practicum.shareit.booking.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.dto.BookingSimpleDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.exception.BookingBadRequestException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    @Autowired
    private BookingService bookingService;

    @PostMapping
    BookingDto addBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                          @RequestBody BookingSimpleDto bookingSimpleDto,
                          BindingResult errors) {
        if (errors.hasErrors()) {
            throw new BookingBadRequestException("Ошибка валидации бронирования: " + bookingSimpleDto);
        }
        log.info("POST запрос для user_id: {}, BookingAddDto: {}", userId, bookingSimpleDto);
        return bookingService.add(userId, bookingSimpleDto);
    }

    @PatchMapping("/{bookingId}")
    BookingDto approveBooking(@PathVariable long bookingId,
                                     @RequestParam(name = "approved") boolean approved,
                                     @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("PATCH запрос для user_id: {}, booking_id: {}, approved={}", bookingId, userId, approved);
        return bookingService.approve(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    BookingDto getBooking(@PathVariable long bookingId,
                          @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("GET запрос для user_id: {}, booking_id: {}", userId, bookingId);
        return bookingService.get(bookingId, userId);
    }

    @GetMapping
    List<BookingDto> getBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                 @RequestParam(name = "state", required = false, defaultValue = "ALL") String state) {
        log.info("GET запрос собственных бронирований пользователя с id: {}, state: {}", userId, state);
        return bookingService.getOwnBookings(userId, state);
    }

    @GetMapping("/owner")
    List<BookingDto> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                      @RequestParam(name = "state", required = false, defaultValue = "ALL") String state) {
        log.info("GET запрос бронирований предметов пользователя с id: {}, state: {}", userId, state);
        return bookingService.getBookingsForOwnItems(userId, state);
    }
}
