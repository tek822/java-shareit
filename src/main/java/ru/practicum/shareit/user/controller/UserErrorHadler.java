package ru.practicum.shareit.user.controller;

import org.modelmapper.ValidationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.user.exception.UserEmailAlreadyExists;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;

import java.util.Map;

@RestControllerAdvice
public class UserErrorHadler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> userNotFound(UserNotFoundException e) {
        return Map.of("Пользователь не найден", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> validationException(ValidationException e) {
        return Map.of("Ошибка валидации данных пользователя", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> emailAlreadyExists(UserEmailAlreadyExists e) {
        return Map.of("Такой email уже используется", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> userValidationFailed(org.springframework.web.bind.MethodArgumentNotValidException e) {
        return Map.of("Ошибка валидации пользователя", e.getMessage());
    }
}
