package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.UpdateForbiddenException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ModelMapper modelMapper;


    @Override
    public ItemDto add(long userId, ItemDto itemDto) {
        User user = userRepository.get(userId);
        Item item = modelMapper.map(itemDto, Item.class);
        item.setOwner(user);
        long itemId = itemRepository.add(item).getId();
        log.info("uid: {}, добавил item с id: {}", userId, itemId);
        itemDto.setId(itemId);
        return itemDto;
    }

    @Override
    public ItemDto update(long userId, ItemDto itemDto) {
        User user = userRepository.get(userId);
        Item item = new Item(itemRepository.get(itemDto.getId()));
        if (userId != item.getOwner().getId()) {
            throw new UpdateForbiddenException("Пользователь с id " + user.getId()
                    + " не является владельцем предмета с id " + itemDto.getId());
        }
        Item update = modelMapper.map(itemDto, Item.class);
        modelMapper.map(update, item);
        log.info("uid: {}, обновил item с id: {}", userId, itemDto.getId());
        return modelMapper.map(itemRepository.update(item), ItemDto.class);
    }

    @Override
    public ItemDto get(long itemId) {
        return modelMapper.map(itemRepository.get(itemId), ItemDto.class);
    }

    @Override
    public Collection<ItemDto> getAll(long userId) {
        User user = userRepository.get(userId);
        log.info("Запрошены items пользователя с uid: {}", userId);
        return itemRepository.getAll(userId).stream()
                .map(i -> modelMapper.map(i, ItemDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemDto> findAvailable(String text) {
        log.info("Поиск предметов со строкой: {}", text);
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        return itemRepository.search(text.toLowerCase()).stream()
                .filter(i -> ((i.getAvailable() != null) && i.getAvailable()))
                .map(i -> modelMapper.map(i, ItemDto.class))
                .collect(Collectors.toList());
    }
}
