package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.CommentDto;
import ru.practicum.shareit.item.ItemDto;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemClient itemClient;
    private ItemDto itemDto = new ItemDto(1L, "Item1", "Item1 description", true, null, null, null, null);
    private long uid = 1L;

    @Test
    void addItemTest() throws Exception {
        when(itemClient.addItem(anyLong(), any(ItemDto.class))).thenReturn(ResponseEntity.ok(itemDto));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", uid)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())));

        verify(itemClient, times(1)).addItem(anyLong(), any(ItemDto.class));
    }

    @Test
    void addItemDtoValidationTest() throws Exception {
        when(itemClient.addItem(anyLong(), any(ItemDto.class))).thenReturn(ResponseEntity.ok(itemDto));

        itemDto.setName("");
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", uid)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        itemDto.setName("Item1");
        itemDto.setDescription("");
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", uid)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        itemDto.setDescription("Item1 description");
        itemDto.setAvailable(null);
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", uid)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        itemDto.setAvailable(true);
        verify(itemClient, never()).addItem(anyLong(), any(ItemDto.class));
    }

    @Test
    void getItemTest() throws Exception {
        when(itemClient.getItem(anyLong(), anyLong())).thenReturn(ResponseEntity.ok(itemDto));

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", uid)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())));

        verify(itemClient, times(1)).getItem(anyLong(), anyLong());
    }

    @Test
    void updateItemTest() throws Exception {
        itemDto.setName("UpdatedName");
        when(itemClient.updateItem(anyLong(), any(ItemDto.class))).thenReturn(ResponseEntity.ok(itemDto));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", uid)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())));

        verify(itemClient, times(1)).updateItem(anyLong(), any(ItemDto.class));
        itemDto.setName("Item1");
    }

    @Test
    void getItemsTest() throws Exception {
        ItemDto itemDto2 = new ItemDto(2L, "Item2", "Item2 description", false, null, null, null, null);
        when(itemClient.getAllItems(anyLong(), anyInt(), anyInt())).thenReturn(ResponseEntity.ok(List.of(itemDto, itemDto2)));

        mockMvc.perform(get("/items?from=0&size=20")
                        .header("X-Sharer-User-Id", uid)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())));

        verify(itemClient, times(1)).getAllItems(uid, 0, 20);
    }

    @Test
    void invalidPaginationTest() throws Exception {
        when(itemClient.getAllItems(anyLong(), anyInt(), anyInt())).thenReturn(ResponseEntity.ok(null));

        mockMvc.perform(get("/items?from=-1&size=20")
                        .header("X-Sharer-User-Id", uid)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/items?from=0&size=0")
                        .header("X-Sharer-User-Id", uid)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());


        verify(itemClient, never()).getAllItems(anyLong(), anyInt(), anyInt());
    }

    @Test
    void validationSearchItemTest() throws Exception {
        when(itemClient.findAvailableItems(anyLong(), anyString(), anyInt(), anyInt())).thenReturn(ResponseEntity.ok(List.of(itemDto)));

        mockMvc.perform(get("/items/search?text=test&from=-1&size=20")
                        .header("X-Sharer-User-Id", uid)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).findAvailableItems(anyLong(), anyString(), anyInt(), anyInt());
    }

    @Test
    void validationSearchItemEmptyStringTest() throws Exception {

        mockMvc.perform(get("/items/search?text=&from=0&size=20")
                        .header("X-Sharer-User-Id", uid)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(itemClient, never()).findAvailableItems(anyLong(), anyString(), anyInt(), anyInt());
    }

    @Test
    void validationSearchItemNullStringTest() throws Exception {

        mockMvc.perform(get("/items/search?from=0&size=20")
                        .header("X-Sharer-User-Id", uid)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(itemClient, never()).findAvailableItems(anyLong(), anyString(), anyInt(), anyInt());
    }

    @Test
    void addCommentValidationTest() throws Exception {
        CommentDto commentDto = new CommentDto(null, "", itemDto.getId(), "comment author", null);
        when(itemClient.addComment(anyLong(), anyLong(), any(CommentDto.class))).thenReturn(ResponseEntity.ok(null));

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", uid)
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).addComment(anyLong(), anyLong(), any(CommentDto.class));
    }

    @Test
    void addItemWithoutXSharerUserIdHeaderTest() throws Exception {

        mockMvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).addItem(anyLong(), any(ItemDto.class));
    }
}
