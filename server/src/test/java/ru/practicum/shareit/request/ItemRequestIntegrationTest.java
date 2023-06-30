package ru.practicum.shareit.request;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "db.name=test", webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test.properties")
class ItemRequestIntegrationTest {
    @Autowired
    private ItemRequestRepository itemRequestRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ItemRequestServiceImpl itemRequestService;

    private User requestor;
    private User owner;
    private Item item;
    private ItemRequest itemRequest;
    private ItemRequestDto itemRequestDto;

    @AfterEach
    void clearDb() {
        itemRequestRepository.deleteAll();
        userRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    void getTest() {
        owner = userRepository.save(new User(0L, "owner", "owner@email.com"));
        requestor = userRepository.save(new User(0L, "requestor", "req@mail.com"));
        itemRequest = itemRequestRepository.save(new ItemRequest(0L, "request for anything", requestor, LocalDateTime.now()));
        item = itemRepository.save(new Item(0L, "item1", "item for request", true, owner, itemRequest));

        itemRequestDto = itemRequestService.get(requestor.getId(), itemRequest.getId());

        assertNotNull(itemRequestDto);
        assertNotEquals(0L, itemRequestDto.getId());
        assertNotEquals(0L, owner.getId());
        assertNotEquals(0L, requestor.getId());

        assertEquals(itemRequestDto.getDescription(), itemRequest.getDescription());
        assertNotNull(itemRequestDto.getCreated());
        assertNotNull(itemRequestDto.getItems());
        assertEquals(itemRequestDto.getItems().get(0), modelMapper.map(item, ItemDto.class));
    }

    @Test
    void getAllTest() {
        int from = 0;
        int size = 20;
        PageRequest page = PageRequest.of(from / size, size);
        owner = userRepository.save(new User(0L, "owner", "owner@email.com"));
        requestor = userRepository.save(new User(0L, "requestor", "req@mail.com"));
        itemRequest = itemRequestRepository.save(new ItemRequest(0L, "request for anything", requestor, LocalDateTime.now()));
        item = itemRepository.save(new Item(0L, "item1", "item for request", true, owner, itemRequest));

        Collection<ItemRequestDto> requests = itemRequestService.getAll(requestor.getId(), from, size);
        assertEquals(0L, requests.size());

        requests = itemRequestService.getAll(owner.getId(), from, size);
        assertEquals(1L, requests.size());
        assertEquals(item.getId(), requests.iterator().next().getItems().get(0).getId());
    }
}
