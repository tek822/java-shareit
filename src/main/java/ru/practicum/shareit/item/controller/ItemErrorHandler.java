package ru.practicum.shareit.item.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.item.exception.UpdateForbiddenException;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ItemErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> updateForbidden(UpdateForbiddenException e) {
        log.info("Изменение запрещено: {}", e.getMessage());
        return Map.of("Изменение запрещено", e.getMessage());
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> missingHeader(MissingRequestHeaderException e) {
        log.info("Отсутствует X-Sharer-User-Id заголовок: {}", e.getMessage());
        return Map.of("Отсутствует X-Sharer-User-Id заголовок", e.getMessage());
    }
}
