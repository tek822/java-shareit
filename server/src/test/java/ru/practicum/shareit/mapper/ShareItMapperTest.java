package ru.practicum.shareit.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.practicum.shareit.ShareItTestsConfiguration;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = { ShareItTestsConfiguration.class })
@ExtendWith({MockitoExtension.class, SpringExtension.class})
class ShareItMapperTest {

    @Autowired
    private ModelMapper modelMapper;

    @Test
    void configurationTest() {
        assertTrue(modelMapper.getConfiguration().isSkipNullEnabled());
    }

    @Test
    void skipNullTest() {
        User owner = new User(1L, "UserName", "user@mail.com");
        Item item = new Item(1L, "ItemName", "ItemDescription", true, owner, null);
        Item update = new Item(1L, "UpdatedName", null, null, null, null);

        modelMapper.map(update, item);

        assertEquals(true, item.getAvailable());
        assertNotNull(item.getOwner());
        assertEquals(owner, item.getOwner());
        assertNotNull(item.getDescription());
        assertEquals("ItemDescription", item.getDescription());
        assertNull(item.getRequest());
        assertEquals("UpdatedName", item.getName());
    }

    @Test
    void converterTest() {
        User owner = new User(1L, "UserName", "user@mail.com");
        User requestor = new User(2L, "notOwnerName", "notOwner@mail.com");
        ItemRequest request = new ItemRequest(11L, "RequestDescription", requestor, LocalDateTime.now());
        Item item = new Item(1L, "ItemName", "ItemDescription", true, owner, null);

        ItemDto itemDto = modelMapper.map(item, ItemDto.class);
        assertNull(itemDto.getRequestId());

        item.setRequest(request);
        ItemDto itemDto2 = modelMapper.map(item, ItemDto.class);
        assertEquals(request.getId(), itemDto2.getRequestId());
    }
}
