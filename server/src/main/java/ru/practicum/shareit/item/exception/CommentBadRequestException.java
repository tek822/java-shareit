package ru.practicum.shareit.item.exception;

public class CommentBadRequestException extends RuntimeException {
    public CommentBadRequestException(String message) {
        super(message);
    }
}
