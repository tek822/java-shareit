package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Collection;

public interface ItemRequestService {

    ItemRequestDto add(long userId, ItemRequestDto itemRequestDto);

    Collection<ItemRequestDto> getOwn(long userId);

    Collection<ItemRequestDto> getAll(long userId, int from, int size);

    ItemRequestDto get(long userId, long itemRequestId);
}
