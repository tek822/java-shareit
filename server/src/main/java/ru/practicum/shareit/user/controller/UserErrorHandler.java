package ru.practicum.shareit.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class UserErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> userNotFound(UserNotFoundException e) {
        log.info("Пользователь не найден: {}", e.getMessage());
        return Map.of("Пользователь не найден", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> constraintViolation(org.springframework.dao.DataIntegrityViolationException e) {
        log.info("Такой email уже используется: {}", e.getMessage());
        return Map.of("Такой email уже используется", e.getMessage());
    }
}
