package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;

import javax.validation.Valid;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository("ItemInMemoryRepository")
@Validated
public class ItemInMemoryRepositoryImp implements ItemRepository {
    long nextId = 1;
    private final Map<Long, Item> items = new HashMap<>();

    @Override
    public Item add(Item item) {
        item.setId(nextId);
        log.info("Добавлен item с id {}", nextId);
        items.put(nextId++, item);
        return item;
    }

    @Override
    public Item update(@Valid Item item) {
        long itemId = item.getId();
        if (!items.containsKey(itemId)) {
            throw new ItemNotFoundException("Предмет с id = " + itemId + " не найден");
        }
        items.replace(itemId, item);
        return item;
    }

    @Override
    public Item get(long itemId) {
        if (!items.containsKey(itemId)) {
            throw new ItemNotFoundException("Предмет с id = " + itemId + " не найден");
        }
        return items.get(itemId);
    }

    @Override
    public Collection<Item> getAll(long userId) {
        return items.values().stream()
                .filter(i -> i.getOwner().getId() == userId)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Item> search(String text) {
        return items.values().stream()
                .filter(i -> (i.getName().toLowerCase().contains(text)
                        || i.getDescription().toLowerCase().contains(text)))
                .collect(Collectors.toList());
    }

    @Override
    public long size() {
        return items.size();
    }
}
