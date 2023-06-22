package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.shareit.util.Util.getItemRequest;
import static ru.practicum.shareit.util.Util.getUser;

@Slf4j
@Service
public class ItemRequestServiceImpl implements ItemRequestService {
    @Autowired
    private ItemRequestRepository itemRequestRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ItemRequestDto add(long userId, ItemRequestDto itemRequestDto) {
        User user = getUser(userRepository, userId);
        ItemRequest itemRequest = modelMapper.map(itemRequestDto, ItemRequest.class);
        itemRequest.setRequestor(user);
        itemRequest.setCreated(LocalDateTime.now());
        log.info("Добавлен запрос от пользователя с id: {}\n{}", userId, itemRequestDto.getDescription());
        return modelMapper.map(itemRequestRepository.save(itemRequest), ItemRequestDto.class);
    }

    @Override
    public Collection<ItemRequestDto> getOwn(long userId) {
        User user = getUser(userRepository, userId);
        log.info("Запрос всех собственных itemRequests от пользователя с id: {}", userId);
        List<ItemRequestDto> itemRequestDtos = itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId).stream()
                .map(itemRequest -> modelMapper.map(itemRequest, ItemRequestDto.class))
                .collect(Collectors.toList());
        return getItemsForRequests(itemRequestDtos);
    }

    @Override
    public Collection<ItemRequestDto> getAll(long userId, int from, int size) {
        User user = getUser(userRepository, userId);
        PageRequest page = PageRequest.of(from / size, size);
        log.info("Запрос всех не собственных itemRequests от пользователя с id: {}", userId);
        List<ItemRequestDto> itemRequestDtos = itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId, page).stream()
                .map(itemRequest -> modelMapper.map(itemRequest, ItemRequestDto.class))
                .collect(Collectors.toList());
        return getItemsForRequests(itemRequestDtos);
    }

    @Override
    public ItemRequestDto get(long userId, long itemRequestId) {
        User user = getUser(userRepository, userId);
        log.info("Запрос itemRequest с id: {}, от пользователя с id: {}", itemRequestId, userId);
        ItemRequestDto itemRequestDto = modelMapper.map(getItemRequest(itemRequestRepository, itemRequestId), ItemRequestDto.class);
        return getItemsForRequests(List.of(itemRequestDto)).stream().findAny().get();
    }

    private Collection<ItemRequestDto> getItemsForRequests(List<ItemRequestDto> requests) {
        Map<Long, ItemRequestDto> requestsMap = new HashMap<>();
        requests.stream().forEach(request -> {
            request.setItems(new ArrayList<>());
            requestsMap.put(request.getId(), request);
        });

        for (Item item : itemRepository.findAllByRequestIdIn(requestsMap.keySet())) {
            ItemRequestDto itemRequestDto = requestsMap.get(item.getRequest().getId());
            ItemDto itemDto = modelMapper.map(item, ItemDto.class);
            itemRequestDto.getItems().add(itemDto);
        }

        return requestsMap.values();
    }
}
