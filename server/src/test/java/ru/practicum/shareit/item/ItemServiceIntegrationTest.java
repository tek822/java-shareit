package ru.practicum.shareit.item;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "db.name=test", webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test.properties")
class ItemServiceIntegrationTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemServiceImpl itemService;
    @Autowired
    private ModelMapper modelMapper;

    private User user = new User(0L, "User", "user@mail.com");
    private Item item1 = new Item(0L, "Item1", "Item1 description", true, user, null);
    private Item item2 = new Item(0L, "Item2", "Item2 description", true, user, null);

    @BeforeEach
    void initDb() {
        user = userRepository.save(user);
        item1.setOwner(user);
        item1 = itemRepository.save(item1);
        item2.setOwner(user);
        item2 = itemRepository.save(item2);
    }

    @AfterEach
    void clearDb() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    void getAllItemsTest() {
        int from = 0;
        int size = 20;
        ItemDto itemDto1 = modelMapper.map(item1, ItemDto.class);
        ItemDto itemDto2 = modelMapper.map(item2, ItemDto.class);

        Collection<ItemDto> itemDtos = itemService.getAll(user.getId(), from, size);

        assertNotNull(itemDtos);
        assertEquals(2, itemDtos.size());
        assertTrue(itemDtos.containsAll(List.of(itemDto1, itemDto2)));
    }
}
