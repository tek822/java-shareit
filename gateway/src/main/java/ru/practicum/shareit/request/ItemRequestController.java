package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@Validated
public class ItemRequestController {
    @Autowired
    private ItemRequestClient itemRequestClient;

    @PostMapping
    ResponseEntity<Object>  addItemRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @Valid @RequestBody ItemRequestDto itemRequestDto) {
        log.info("POST запрос от пользователя с id: {}\n{}", userId, itemRequestDto.getDescription());
        return itemRequestClient.addItemRequest(userId, itemRequestDto);
    }

    @GetMapping
    ResponseEntity<Object> getOwnItemRequests(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("GET запрос всех собственных запросов пользователя с id:{}", userId);
        return itemRequestClient.getOwn(userId);
    }

    @GetMapping("/all")
    ResponseEntity<Object>  getItemRequests(@RequestHeader("X-Sharer-User-Id") long userId,
                                    @PositiveOrZero(message = "Ошибка пагинации, from >= 0")
                                    @RequestParam(name = "from", required = false, defaultValue = "0") int from,
                                    @Positive(message = "Ошибка пагинации, size > 0")
                                    @RequestParam(name = "size", required = false, defaultValue = "20") int size) {
        log.info("GET запрос всех запросов для пользователя с id:{}", userId);
        return itemRequestClient.getAll(userId, from, size);
    }

    @GetMapping("{requestId}")
    ResponseEntity<Object>  getRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                              @PathVariable long requestId) {
        log.info("GET запрос для пользователя с id: {}, запроса с id: {}", userId, requestId);
        return itemRequestClient.getItemRequest(userId, requestId);
    }
}
