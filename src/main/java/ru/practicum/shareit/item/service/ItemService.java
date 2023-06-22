package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {

    ItemDto add(long userId, ItemDto itemDto);

    ItemDto update(long userId, ItemDto itemDto);

    ItemDto get(long itemId, long userId);

    Collection<ItemDto> getAll(long userId, int from, int size);

    Collection<ItemDto> findAvailable(String text, int from, int size);

    CommentDto addComment(long itemId, long userId, CommentDto commentDto);
}
