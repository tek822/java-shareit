package ru.practicum.shareit.item;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.UpdateForbiddenException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class ItemServiceTest {
    private ItemService itemService;
    private ItemRepository itemRepository;
    private UserRepository userRepository;
    private static long uid1;
    private static long uid2;
    private static long itemId1;
    private static long itemId2;

    @Autowired
    public ItemServiceTest(ItemService itemService,
                           ItemRepository itemRepository,
                           UserRepository userRepository) {
        this.itemService = itemService;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Test
    @Order(1)
    void createFirstItemTest() {
        uid1 = userRepository.add(new User(-1L, "user1", "user1@mail.com")).getId();
        uid2 = userRepository.add(new User(-1L, "user2", "user2@mail.com")).getId();
        ItemDto itemDto1 = new ItemDto(-1L, "item1", "item1 description", false);

        itemId1 = itemService.add(uid1, itemDto1).getId();
        Item item = itemRepository.get(itemId1);

        assertEquals(itemRepository.size(), 1, "В хранилище должен добаиться один предмет");
        assertEquals(item.getAvailable(), false, "Available == false");
        assertEquals(item.getDescription(), "item1 description", "Description == item1 description");
        assertEquals(item.getName(), "item1", "Name == item1");
        assertEquals(item.getOwner().getId(), uid1, "Owner.uid == " + uid1);
    }

    @Test
    @Order(2)
    void createWithNotFoundUserTest() {
        ItemDto itemDto2 = new ItemDto(-1L, "item2", "item2 description", false);

        assertThrows(UserNotFoundException.class, () -> itemService.add(-1L, itemDto2),
                "Для добавления предмета пользователь должен существовать в базе");
    }

    @Test
    @Order(3)
    void fullItemUpdateTest() {
        ItemDto itemDto = new ItemDto(itemId1, "item1+", "item1 description+", true);

        itemService.update(uid1, itemDto);
        Item item = itemRepository.get(itemId1);

        assertEquals(item.getAvailable(), true, "Available == true");
        assertEquals(item.getDescription(), "item1 description+", "Description == item1 description+");
        assertEquals(item.getName(), "item1+", "Name == item1+");
    }

    @Test
    @Order(4)
    void updateItemWithOtherUserTest() {
        ItemDto itemDto = new ItemDto(itemId1, "item1-", "item1 description-", false);
        assertThrows(UpdateForbiddenException.class, () -> itemService.update(uid2, itemDto),
                "Пользователь должен быть владельцем предмета для обновления");
    }

    @Test
    @Order(5)
    void updateAvailableFieldTest() {
        ItemDto itemDto = new ItemDto(itemId1, null, null, false);

        itemService.update(uid1, itemDto);
        Item item = itemRepository.get(itemId1);

        assertEquals(item.getAvailable(), false, "Available == false");
        assertEquals(item.getDescription(), "item1 description+","Description == item1 description+");
        assertEquals(item.getName(), "item1+", "Name == item1+");
    }

    @Test
    @Order(6)
    void updateDescriptionFieldTest() {
        ItemDto itemDto = new ItemDto(itemId1, null, "item1 description-", null);

        itemService.update(uid1, itemDto);
        Item item = itemRepository.get(itemId1);

        assertEquals(item.getAvailable(), false, "Available == false");
        assertEquals(item.getDescription(), "item1 description-","Description == item1 description-");
        assertEquals(item.getName(), "item1+", "Name == item1+");
    }

    @Test
    @Order(7)
    void updateNameFieldTest() {
        ItemDto itemDto = new ItemDto(itemId1, "item1-", null, null);

        itemService.update(uid1, itemDto);
        Item item = itemRepository.get(itemId1);

        assertEquals(item.getAvailable(), false, "Available == false");
        assertEquals(item.getDescription(), "item1 description-", "Description == item1 description-");
        assertEquals(item.getName(), "item1-", "Name == item1-");
    }

    @Test
    @Order(8)
    void getItemTest() {
        ItemDto itemDto = itemService.get(itemId1);
        Item item = itemRepository.get(itemId1);

        assertEquals(itemDto.getAvailable(), false, "Available == false");
        assertEquals(itemDto.getDescription(), "item1 description-", "Description == item1 description-");
        assertEquals(itemDto.getName(), "item1-","Name == item1-");
        assertEquals(item.getOwner().getId(), uid1,"item.owner.uid == " + uid1);
    }

    @Test
    @Order(9)
    void createSecondItemTest() {
        ItemDto itemDto2 = new ItemDto(-1L, "item2", "item2 description", true);

        itemId2 = itemService.add(uid2, itemDto2).getId();
        Item item = itemRepository.get(itemId2);

        assertEquals(itemRepository.size(), 2, "В хранилище должен быть два предмета");
        assertEquals(item.getAvailable(), true, "Available == true");
        assertEquals(item.getDescription(), "item2 description", "Description == item2 description");
        assertEquals(item.getName(), "item2", "Name == item2");
        assertEquals(item.getOwner().getId(), uid2, "Owner.uid == " + uid2);
    }

    @Test
    @Order(10)
    void getAllUser1Test() {
        Collection<ItemDto> items = itemService.getAll(uid1);
        assertEquals(items.size(), 1, "У пользователя должен быть один предмет");
        assertEquals(itemId1, items.iterator().next().getId(), "itemId == " + itemId1);
    }

    @Test
    @Order(11)
    void getAllUser2Test() {
        Collection<ItemDto> items = itemService.getAll(uid2);

        assertEquals(items.size(), 1, "У пользователя должен быть один предмет");
        assertEquals(itemId2, items.iterator().next().getId(), "itemId == " + itemId2);
    }

    @Test
    @Order(12)
    void searchOneAvailableItemTest() {
        Collection<ItemDto> items = itemService.findAvailable("desc");

        assertEquals(1L, items.size(), "Доступен один предмет");
        assertEquals(itemId2, items.iterator().next().getId(), "itemId == " + itemId2);
    }

    @Test
    @Order(13)
    void searchWithEmptyStringTest() {
        Collection<ItemDto> items = itemService.findAvailable("");

        assertEquals(0L, items.size(), "Список должен быть пуст");
    }

    @Test
    @Order(14)
    void searchTwoAvailableItemsTest() {
        ItemDto itemDto1 = new ItemDto(itemId1, null, null, true);
        itemService.update(uid1, itemDto1);

        Collection<ItemDto> items = itemService.findAvailable("desc");

        assertEquals(2L, items.size(), "Доступно два предмет");
    }
}
