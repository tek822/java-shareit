package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Collection;

public interface ItemRequestService {

    ItemRequestDto addItemRequest(long userId, ItemRequestDto itemRequestDto);

    Collection<ItemRequestDto> getOwnItemRequests(long userId);

    Collection<ItemRequestDto> getItemRequests(long userId, int from, int size);

    ItemRequestDto getItemRequest(long userId, long itemRequestId);
}
