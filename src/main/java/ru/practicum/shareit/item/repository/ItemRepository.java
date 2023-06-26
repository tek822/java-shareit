package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;


public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("select item from Item as item join item.owner as u where u.id = ?1")
    List<Item> findAllByUserId(long userId);

    @Query("select item from Item as item join item.owner as u where u.id = ?1")
    List<Item> findAllByUserId(long userId, Pageable page);

    List<Item> findAllByDescriptionContainingIgnoreCaseOrNameContainingIgnoreCase(String text1, String text2, Pageable page);

    Collection<Item> findAllByRequestIdIn(Collection<Long> requestsIds);
}
