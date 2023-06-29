package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    private static final String MESSAGE = "validation error";

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> validationError(ValidationException e) {
        log.info(MESSAGE, e.getMessage());
        return Map.of(MESSAGE, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> constraintViolationError(ConstraintViolationException e) {
        log.info(MESSAGE, e.getMessage());
        return Map.of(MESSAGE, e.getMessage());
    }

    // Error in boolean variable
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> methodArgumentViolationError(MethodArgumentTypeMismatchException e) {
        log.info(MESSAGE, e.getMessage());
        return Map.of(MESSAGE, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> userValidationFailed(org.springframework.web.bind.MethodArgumentNotValidException e) {
        log.info("Ошибка валидации: {}", e.getMessage());
        return Map.of("Ошибка валидации ", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> missingHeader(MissingRequestHeaderException e) {
        log.info("Отсутствует X-Sharer-User-Id заголовок: {}", e.getMessage());
        return Map.of("Отсутствует X-Sharer-User-Id заголовок", e.getMessage());
    }
}
