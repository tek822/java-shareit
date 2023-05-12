package ru.practicum.shareit.item.exception;

public class UpdateForbiddenException extends RuntimeException {
    public UpdateForbiddenException() {
        super();
    }

    public UpdateForbiddenException(String message) {
        super(message);
    }
}
