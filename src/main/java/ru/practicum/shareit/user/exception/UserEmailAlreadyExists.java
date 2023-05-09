package ru.practicum.shareit.user.exception;

public class UserEmailAlreadyExists extends RuntimeException {

    public UserEmailAlreadyExists() {
        super();
    }

    public UserEmailAlreadyExists(String message) {
        super(message);
    }
}
