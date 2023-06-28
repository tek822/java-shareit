package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.ValidationException;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.booking.BookingDtoValidator.isValid;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@Validated
public class BookingController {
    @Autowired
    private BookingClient bookingClient;

    @PostMapping
    ResponseEntity<Object> addBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                          @Valid @RequestBody BookingSimpleDto bookingSimpleDto,
                          BindingResult errors) {
        if (errors.hasErrors() || !isValid(bookingSimpleDto)) {
            throw new ValidationException("Ошибка валидации бронирования: " + bookingSimpleDto);
        }
        log.info("POST запрос для user_id: {}, BookingAddDto: {}", userId, bookingSimpleDto);
        return bookingClient.addBooking(userId, bookingSimpleDto);
    }

    @PatchMapping("/{bookingId}")
    ResponseEntity<Object> approveBooking(@PathVariable long bookingId,
                              @RequestParam(name = "approved") boolean approved,
                              @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("PATCH запрос для user_id: {}, booking_id: {}, approved={}", bookingId, userId, approved);
        return bookingClient.approveBooking(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    ResponseEntity<Object> getBooking(@PathVariable long bookingId,
                          @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("GET запрос для user_id: {}, booking_id: {}", userId, bookingId);
        return bookingClient.getBooking(bookingId, userId);
    }

    @GetMapping
    ResponseEntity<Object> getBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                 @RequestParam(name = "state", required = false, defaultValue = "ALL") String state,
                                 @PositiveOrZero(message = "Ошибка пагинации, from >= 0")
                                 @RequestParam(name = "from", required = false, defaultValue = "0") int from,
                                 @Positive(message = "Ошибка пагинации, size > 0")
                                 @RequestParam(name = "size", required = false, defaultValue = "20") int size) {
        log.info("GET запрос собственных бронирований пользователя с id: {}, state: {}", userId, state);
        return bookingClient.getOwnBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    ResponseEntity<Object> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                    @RequestParam(name = "state", required = false, defaultValue = "ALL") String state,
                                    @PositiveOrZero(message = "Ошибка пагинации, from >= 0")
                                    @RequestParam(name = "from", required = false, defaultValue = "0") int from,
                                    @Positive(message = "Ошибка пагинации, size > 0")
                                    @RequestParam(name = "size", required = false, defaultValue = "20") int size) {
        log.info("GET запрос бронирований предметов пользователя с id: {}, state: {}", userId, state);
        return bookingClient.getBookingsForOwnItems(userId, state, from, size);
    }
}
