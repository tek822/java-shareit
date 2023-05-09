package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemRepository {

    Item add(Item item);

    Item update(Item item);

    Item get(long id);

    Collection<Item> getAll(long userId);

    Collection<Item> search(String text);

    long size();
}
