package ru.practicum.shareit.request.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    @Autowired
    private ItemRequestService itemRequestService;

    @PostMapping
    ItemRequestDto addItemRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @Valid @RequestBody ItemRequestDto itemRequestDto) {
        log.info("POST запрос от пользователя с id: {}\n{}", userId, itemRequestDto.getDescription());
        return itemRequestService.addItemRequest(userId, itemRequestDto);
    }

    @GetMapping
    Collection<ItemRequestDto> getOwnItemRequests(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("GET запрос всех собственных запросов пользователя с id:{}", userId);
        return itemRequestService.getOwnItemRequests(userId);
    }

    @GetMapping("/all")
    Collection<ItemRequestDto> getItemRequests(@RequestHeader("X-Sharer-User-Id") long userId,
                                               @RequestParam(name = "from", required = false, defaultValue = "0") int from,
                                               @RequestParam(name = "size", required = false, defaultValue = "20") int size) {
        log.info("GET запрос всех запросов для пользователя с id:{}", userId);
        return itemRequestService.getItemRequests(userId, from, size);
    }

    @GetMapping("{requestId}")
    ItemRequestDto getRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                              @PathVariable long requestId) {
        log.info("GET запрос для пользователя с id: {}, запроса с id: {}", userId, requestId);
        return itemRequestService.getItemRequest(userId, requestId);
    }
}
