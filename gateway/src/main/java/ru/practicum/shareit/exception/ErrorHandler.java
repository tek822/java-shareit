package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    private static final String MESSAGE = "validation error {}";

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse validationError(ValidationException e) {
        log.info(MESSAGE, e.getMessage());
        return new ErrorResponse(e.getMessage());
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
        log.info(MESSAGE, e.getMessage());
        return Map.of(MESSAGE, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> missingHeader(MissingRequestHeaderException e) {
        log.info("Отсутствует X-Sharer-User-Id заголовок: {}", e.getMessage());
        return Map.of("Отсутствует X-Sharer-User-Id заголовок", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> restClientError(RestClientException e) {
        log.info("Ошибка при общении с shareit-server: {}", e.getMessage());
        return Map.of("Ошибка при общении с shareit-server", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> generalError(Throwable e) {
        log.info("Internal server error: {}", e.getMessage());
        return Map.of("Internal server error", e.getMessage());
    }
}
