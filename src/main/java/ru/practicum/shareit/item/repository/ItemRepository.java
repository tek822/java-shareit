package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;


public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("select item from Item as item join item.owner as u where u.id = ?1")
    List<Item> findAllByUserId(long userID);

    List<Item> findAllByDescriptionContainingIgnoreCaseOrNameContainingIgnoreCase(String text1, String text2);
}
