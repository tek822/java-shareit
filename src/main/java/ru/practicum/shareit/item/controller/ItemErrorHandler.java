package ru.practicum.shareit.item.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.item.exception.CommentBadRequestException;
import ru.practicum.shareit.item.exception.UpdateForbiddenException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ItemErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> itemNotFound(ItemNotFoundException e) {
        log.info("Предмет не найден: {}", e.getMessage());
        return Map.of("Предмет не найден", e.getMessage());
    }

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

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> commentBadRequest(CommentBadRequestException e) {
        log.info("comment error: {}", e.getMessage());
        return Map.of("comment error", e.getMessage());
    }
}
