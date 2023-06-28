package ru.practicum.shareit.item;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Transactional
class ItemJpaTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User user1 = new User(0L, "User1", "user1@mail.com");
    private User user2 = new User(0L, "User2", "user2@mail.com");
    private ItemRequest request1 = new ItemRequest(0L, "request from user2", user2, LocalDateTime.now());
    private ItemRequest request2 = new ItemRequest(0L, "request from user1", user1, LocalDateTime.now());
    private Item item1 = new Item(0L, "Item1", "Item1 description", true, user1, request1);
    private Item item2 = new Item(0L, "Item2", "Item2 description", true, user2, request2);

    @BeforeEach
    void initDb() {
        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);
        request1.setRequestor(user1);
        request1 = itemRequestRepository.save(request1);
        request2.setRequestor(user2);
        request2 = itemRequestRepository.save(request2);
        item1.setOwner(user1);
        item1.setRequest(request1);
        item1 = itemRepository.save(item1);
        item2.setOwner(user2);
        item2.setRequest(request2);
        item2 = itemRepository.save(item2);
    }

    @AfterEach
    void clearDb() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
        itemRequestRepository.deleteAll();
    }

    @Test
    void findAllByUserIdTest() {
        Item item3 = new Item(0L, "Item3", "Item3 description", true, user1, null);
        item3 = itemRepository.save(item3);
        List<Item> items = itemRepository.findAllByUserId(user1.getId());

        assertNotNull(items);
        assertEquals(2L, items.size());
        assertEquals(user1.getId(), items.get(0).getOwner().getId());
    }

    @Test
    void findAllByUserIdWithPaginationTest() {
        Item item3 = new Item(3L, "Item3", "Item3 description", true, user1, null);
        item3 = itemRepository.save(item3);

        List<Item> items = itemRepository.findAllByUserId(user1.getId(), PageRequest.of(0, 1));

        assertNotNull(items);
        assertEquals(1L, items.size());
        assertEquals(user1.getId(), items.get(0).getOwner().getId());
    }

    @Test
    void searchItemTest() {
        Item item3 = new Item(3L, "IDescRipt", "Item3 text", true, user1, null);
        item3 = itemRepository.save(item3);
        String searchText = "DeSc";

        List<Item> items = itemRepository.findAllByDescriptionContainingIgnoreCaseOrNameContainingIgnoreCase(
                searchText, searchText, PageRequest.of(0, 5));

        assertNotNull(items);
        assertEquals(3L, items.size());
    }

    @Test
    void findAllByRequestIdInTest() {
        Item item3 = new Item(3L, "IDescRipt", "Item3 text", true, user1, null);
        item3 = itemRepository.save(item3);

        Collection<Item> items = itemRepository.findAllByRequestIdIn(List.of(request1.getId(), request2.getId()));

        assertNotNull(items);
        assertEquals(2L, items.size());
    }
}
