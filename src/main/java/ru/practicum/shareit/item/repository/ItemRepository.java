package ru.practicum.shareit.item.repository;

import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.item.model.Item;

import javax.validation.Valid;
import java.util.Collection;

@Validated
public interface ItemRepository {

    Item add(Item item);

    Item update(@Valid Item item);

    Item get(long id);

    Collection<Item> getAll(long userId);

    Collection<Item> search(String text);

    long size();
}
