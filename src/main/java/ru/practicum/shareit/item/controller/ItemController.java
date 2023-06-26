package ru.practicum.shareit.item.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/items")
@Validated
public class ItemController {
    @Autowired
    private ItemService itemService;

    @PostMapping
    public ItemDto addItem(@RequestHeader("X-Sharer-User-Id") long userId,
                           @Valid @RequestBody ItemDto itemDto) {
        log.info("POST запрос для id: {}, item: {}", userId, itemDto);
        return itemService.add(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@PathVariable long itemId,
                          @RequestHeader("X-Sharer-User-Id") long userId,
                          @Valid @RequestBody CommentDto commentDto) {
        log.info("POST запрос для id: {}, comment: {}", userId, commentDto);
        return itemService.addComment(itemId, userId, commentDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable long itemId,
                              @RequestHeader("X-Sharer-User-Id") long userId,
                              @RequestBody ItemDto itemDto) {
        itemDto.setId(itemId);
        log.info("PATCH запрос для uid: {}, item: {}", userId, itemDto);
        return itemService.update(userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable long itemId,
                           @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("GET запрос для itemId: {}, от userId: {}", itemId, userId);
        return itemService.get(itemId, userId);
    }

    @GetMapping
    public Collection<ItemDto> getAllItems(@RequestHeader("X-Sharer-User-Id") long userId,
                                           @PositiveOrZero(message = "Ошибка пагинации, from >= 0")
                                           @RequestParam(name = "from", required = false, defaultValue = "0") int from,
                                           @Positive(message = "Ошибка пагинации, size > 0")
                                           @RequestParam(name = "size", required = false, defaultValue = "20") int size) {
        log.info("GET запрос для всех предметов");
        return itemService.getAll(userId, from, size);
    }

    @GetMapping("/search")
    public Collection<ItemDto> findAvailableItems(@RequestParam(name = "text") String text,
                                                  @PositiveOrZero(message = "Ошибка пагинации, from >= 0")
                                                  @RequestParam(name = "from", required = false, defaultValue = "0") int from,
                                                  @Positive(message = "Ошибка пагинации, size > 0")
                                                  @RequestParam(name = "size", required = false, defaultValue = "20") int size) {
        log.info("GET запрос на поиск с text: {}", text);
        return itemService.findAvailable(text, from, size);
    }
}
