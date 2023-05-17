package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingSimpleDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto add(long userId, BookingSimpleDto bookingDto);
    BookingDto approve(long bookingId, long userId, boolean approve);
    BookingDto get(long bookingId, long userId);
    List<BookingDto> getOwnBookings(long userId, String state);
    List<BookingDto> getBookingsForOwnItems(long userId, String state);
}
