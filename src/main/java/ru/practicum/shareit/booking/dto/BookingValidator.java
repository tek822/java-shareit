package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

public class BookingValidator {

   private BookingValidator() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isValid(BookingSimpleDto bookingSimpleDto) {
        return !(isNull(bookingSimpleDto)
                || isEndBeforeStart(bookingSimpleDto)
                || isStartInPast(bookingSimpleDto)
                || isEndInPast(bookingSimpleDto)
                || isStartEqualsEnd(bookingSimpleDto)
            );
    }

    private static boolean isNull(BookingSimpleDto bookingSimpleDto) {
       return (bookingSimpleDto.getStart() == null) || (bookingSimpleDto.getEnd() == null);
    }

    private static boolean isStartEqualsEnd(BookingSimpleDto bookingSimpleDto) {
        return bookingSimpleDto.getStart().isEqual(bookingSimpleDto.getEnd());
    }

    private static boolean isEndInPast(BookingSimpleDto bookingSimpleDto) {
        return bookingSimpleDto.getEnd().isBefore(LocalDateTime.now());
    }

    private static boolean isStartInPast(BookingSimpleDto bookingSimpleDto) {
        return bookingSimpleDto.getStart().isBefore(LocalDateTime.now());
    }

    private static boolean isEndBeforeStart(BookingSimpleDto bookingSimpleDto) {
        return bookingSimpleDto.getEnd().isBefore(bookingSimpleDto.getStart());
    }
}
