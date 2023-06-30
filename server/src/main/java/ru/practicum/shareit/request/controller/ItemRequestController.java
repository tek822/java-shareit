package ru.practicum.shareit.request.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    @Autowired
    private ItemRequestService itemRequestService;

    @PostMapping
    ItemRequestDto addItemRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @RequestBody ItemRequestDto itemRequestDto) {
        log.info("POST запрос от пользователя с id: {}\n{}", userId, itemRequestDto.getDescription());
        return itemRequestService.add(userId, itemRequestDto);
    }

    @GetMapping
    Collection<ItemRequestDto> getOwnItemRequests(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("GET запрос всех собственных запросов пользователя с id:{}", userId);
        return itemRequestService.getOwn(userId);
    }

    @GetMapping("/all")
    Collection<ItemRequestDto> getItemRequests(@RequestHeader("X-Sharer-User-Id") long userId,
                                               @RequestParam(name = "from") int from,
                                               @RequestParam(name = "size") int size) {
        log.info("GET запрос всех запросов для пользователя с id:{}", userId);
        return itemRequestService.getAll(userId, from, size);
    }

    @GetMapping("{requestId}")
    ItemRequestDto getRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                              @PathVariable long requestId) {
        log.info("GET запрос для пользователя с id: {}, запроса с id: {}", userId, requestId);
        return itemRequestService.get(userId, requestId);
    }
}
