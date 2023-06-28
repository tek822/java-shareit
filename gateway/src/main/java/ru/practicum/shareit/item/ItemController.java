package ru.practicum.shareit.item;

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
@RequestMapping("/items")
@Validated
public class ItemController {
    @Autowired
    private ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> addItem(@RequestHeader("X-Sharer-User-Id") long userId,
                           @Valid @RequestBody ItemDto itemDto) {
        log.info("POST запрос для id: {}, item: {}", userId, itemDto);
        return itemClient.addItem(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@PathVariable long itemId,
                                 @RequestHeader("X-Sharer-User-Id") long userId,
                                 @Valid @RequestBody CommentDto commentDto) {
        log.info("POST запрос для id: {}, comment: {}", userId, commentDto);
        return itemClient.addComment(itemId, userId, commentDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@PathVariable long itemId,
                              @RequestHeader("X-Sharer-User-Id") long userId,
                              @RequestBody ItemDto itemDto) {
        itemDto.setId(itemId);
        log.info("PATCH запрос для uid: {}, item: {}", userId, itemDto);
        return itemClient.updateItem(userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@PathVariable long itemId,
                           @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("GET запрос для itemId: {}, от userId: {}", itemId, userId);
        return itemClient.getItem(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItems(@RequestHeader("X-Sharer-User-Id") long userId,
                                           @PositiveOrZero(message = "Ошибка пагинации, from >= 0")
                                           @RequestParam(name = "from", required = false, defaultValue = "0") int from,
                                           @Positive(message = "Ошибка пагинации, size > 0")
                                           @RequestParam(name = "size", required = false, defaultValue = "20") int size) {
        log.info("GET запрос для всех предметов");
        return itemClient.getAllItems(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> findAvailableItems(@RequestHeader("X-Sharer-User-Id") long userId,
                                                 @RequestParam(name = "text") String text,
                                                 @PositiveOrZero(message = "Ошибка пагинации, from >= 0")
                                                 @RequestParam(name = "from", required = false, defaultValue = "0") int from,
                                                 @Positive(message = "Ошибка пагинации, size > 0")
                                                 @RequestParam(name = "size", required = false, defaultValue = "20") int size) {
        log.info("GET запрос на поиск с text: {}", text);
        return itemClient.findAvailableItems(userId, text, from, size);
    }
}
