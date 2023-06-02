package ru.practicum.shareit.request.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.request.exception.BadRequestException;
import ru.practicum.shareit.request.exception.ItemRequestNotFoundException;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ItemRequestErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> badRequest(BadRequestException e) {
        log.info("BAD_REQUEST", e.getMessage());
        return Map.of("BAD_REQUEST", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> itemRequestNotFound(ItemRequestNotFoundException e) {
        log.info("BAD_REQUEST", e.getMessage());
        return Map.of("BAD_REQUEST", e.getMessage());
    }
}
