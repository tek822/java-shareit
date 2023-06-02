package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.exception.BadRequestException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

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
    public ItemRequestDto addItemRequest(long userId, ItemRequestDto itemRequestDto) {
        User user = getUser(userRepository, userId);
        ItemRequest itemRequest = modelMapper.map(itemRequestDto, ItemRequest.class);
        itemRequest.setRequestor(user);
        itemRequest.setCreated(LocalDateTime.now());
        log.info("Добавлен запрос от пользователя с id: {}\n{}", userId, itemRequestDto.getDescription());
        return modelMapper.map(itemRequestRepository.save(itemRequest), ItemRequestDto.class);
    }

    @Override
    public Collection<ItemRequestDto> getOwnItemRequests(long userId) {
        User user = getUser(userRepository, userId);
        log.info("Запрос всех собственных itemRequests от пользователя с id: {}", userId);
        return itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId).stream()
                        .map(itemRequest -> modelMapper.map(itemRequest, ItemRequestDto.class))
                        .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemRequestDto> getItemRequests(long userId, int from, int size) {
        if (from < 0 || size < 1) {
            throw new BadRequestException(String.format("Ошибка в параметрах пагинации, from: %d, size: %d", from, size));
        }
        User user = getUser(userRepository, userId);
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        log.info("Запрос всех не собственных itemRequests от пользователя с id: {}", userId);
        return itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId, page).stream()
                .map(itemRequest -> modelMapper.map(itemRequest, ItemRequestDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getItemRequest(long userId, long itemRequestId) {
        User user = getUser(userRepository, userId);
        log.info("Запрос itemRequest с id: {}, от пользователя с id: {}", itemRequestId, userId);
        return modelMapper.map(itemRequestRepository.findById(itemRequestId),ItemRequestDto.class);
    }
}
