package ru.practicum.shareit.util;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

public class Util {

    private Util() {
        throw new IllegalStateException("Utility class");
    }

    public static User getUser(UserRepository userRepository, long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("Не найден пользователь с id:" + userId)
        );
    }

    public static Item getItem(ItemRepository itemRepository, long itemId) {
        return itemRepository.findById(itemId).orElseThrow(
                () -> new ItemNotFoundException("Не найден предмет с id:" + itemId)
        );
    }

    public static Booking getBooking(BookingRepository bookingRepository, long bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(
                () -> new BookingNotFoundException("Не найдено резервирование с id:" + bookingId)
        );
    }
}
