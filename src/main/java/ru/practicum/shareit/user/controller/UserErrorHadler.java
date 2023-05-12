package ru.practicum.shareit.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ValidationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.user.exception.UserEmailAlreadyExists;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class UserErrorHadler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> userNotFound(UserNotFoundException e) {
        log.info("Пользователь не найден: {}", e.getMessage());
        return Map.of("Пользователь не найден", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> validationException(ValidationException e) {
        log.info("Ошибка валидации данных пользователя: {}", e.getMessage());
        return Map.of("Ошибка валидации данных пользователя", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> emailAlreadyExists(UserEmailAlreadyExists e) {
        log.info("Такой email уже используется: {}", e.getMessage());
        return Map.of("Такой email уже используется", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> userValidationFailed(org.springframework.web.bind.MethodArgumentNotValidException e) {
        log.info("Ошибка валидации пользователя: {}", e.getMessage());
        return Map.of("Ошибка валидации пользователя", e.getMessage());
    }
}
