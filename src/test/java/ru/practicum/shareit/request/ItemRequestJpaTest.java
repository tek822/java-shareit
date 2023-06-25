package ru.practicum.shareit.request;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.model.User;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
class ItemRequestJpaTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User owner = new User(0L, "owner", "owner@mail.com");
    private User requestor = new User(0L, "requestor", "requestor@mail.com");
    private ItemRequest request1 = new ItemRequest(0L, "request from requestor", requestor, LocalDateTime.now());
    private ItemRequest request2 = new ItemRequest(0L, "request2 from requestor", requestor, LocalDateTime.now().plusHours(1));
    private ItemRequest request3 = new ItemRequest(0L, "request from owner", owner, LocalDateTime.now());
    private ItemRequest request4 = new ItemRequest(0L, "request2 from owner", owner, LocalDateTime.now().plusHours(1));

    @BeforeEach
    void initDb() {
        owner = userRepository.save(owner);
        requestor = userRepository.save(requestor);

        request1.setRequestor(requestor);
        request1 = itemRequestRepository.save(request1);

        request2.setRequestor(requestor);
        request2 = itemRequestRepository.save(request2);

        request3.setRequestor(owner);
        request3 = itemRequestRepository.save(request3);

        request4.setRequestor(owner);
        request4 = itemRequestRepository.save(request4);
    }

    @AfterEach
    void clearDb() {
        itemRequestRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findAllByRequestorIdTest() {
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(requestor.getId());

        assertEquals(2L, requests.size());
        assertTrue(requests.containsAll(List.of(request1, request2)));
        assertEquals(request2.getId(), requests.get(0).getId());
    }

    @Test
    void findAllByRequestorIdNotTest() {
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(requestor.getId(), PageRequest.of(0, 20));

        assertEquals(2L, requests.size());
        assertTrue(requests.containsAll(List.of(request3, request4)));
        assertEquals(request4.getId(), requests.get(0).getId());
    }

    @Test
    void findAllByRequestorIdNotPagingTest() {
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(requestor.getId(), PageRequest.of(1, 1));

        assertEquals(1L, requests.size());
        assertTrue(requests.contains(request3));
    }
}
