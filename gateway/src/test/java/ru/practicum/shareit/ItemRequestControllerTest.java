package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemRequestClient itemRequestClient;

    private ItemRequestDto requestDto = new ItemRequestDto(0L, "request description", LocalDateTime.now(), null);
    private long uid = 1L;

    @Test
    void addItemRequestWithEmptyDescriptionTest() throws Exception {
        ItemRequestDto requestDto = new ItemRequestDto(0L, "", LocalDateTime.now(), null);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", uid)
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).addItemRequest(anyLong(), any());
    }


    @Test
    void getAllWithNegativeFromTest() throws Exception {
        mockMvc.perform(get("/requests/all?from=-1&size=1")
                        .header("X-Sharer-User-Id", uid)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).getAll(anyLong(), anyInt(), anyInt());
    }

    @Test
    void getAllWithZeroPageSizeErrorTest() throws Exception {
        mockMvc.perform(get("/requests/all?from=0&size=0")
                        .header("X-Sharer-User-Id", uid)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).getAll(anyLong(), anyInt(), anyInt());
    }
}
