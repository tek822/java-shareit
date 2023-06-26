package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemRequestServiceImpl itemRequestService;

    private ItemRequestDto requestDto = new ItemRequestDto(0L, "request description", LocalDateTime.now(), null);
    private long uid = 1L;

    @Test
    void addItemRequestWithWrongUidTest() throws Exception {
        when(itemRequestService.add(anyLong(), any(ItemRequestDto.class))).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", uid)
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(itemRequestService, times(1)).add(anyLong(), any(ItemRequestDto.class));
    }

    @Test
    void addItemRequestTest() throws Exception {
        when(itemRequestService.add(anyLong(), any(ItemRequestDto.class))).thenReturn(requestDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", uid)
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())));

        verify(itemRequestService, times(1)).add(anyLong(), any(ItemRequestDto.class));
    }

    @Test
    void getOwnRequestsWithWrongUidTest() throws Exception {
        when(itemRequestService.getOwn(anyLong())).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", uid)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(itemRequestService, times(1)).getOwn(anyLong());
    }

    @Test
    void getOwnRequestsTest() throws Exception {
        when(itemRequestService.getOwn(anyLong())).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", uid)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestDto.getDescription())));

        verify(itemRequestService, times(1)).getOwn(anyLong());
    }

    @Test
    void getItemRequestWithWrongUidTest() throws Exception {
        when(itemRequestService.get(anyLong(), anyLong())).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", uid)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(itemRequestService, times(1)).get(anyLong(), anyLong());
    }

    @Test
    void getItemRequestTest() throws Exception {
        when(itemRequestService.get(anyLong(), anyLong())).thenReturn(requestDto);

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", uid)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())));

        verify(itemRequestService, times(1)).get(anyLong(), anyLong());
    }

    @Test
    void getItemRequestWithWrongIdTest() throws Exception {
        when(itemRequestService.get(anyLong(), anyLong())).thenThrow(new ItemRequestNotFoundException("ItemRequest not found"));

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", uid)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(itemRequestService, times(1)).get(anyLong(), anyLong());
    }

    //itemRequestService.getAll(userId, from, size);
    @Test
    void getAllWithWrongUidTest() throws Exception {
        when(itemRequestService.getAll(anyLong(), anyInt(), anyInt())).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", uid)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(itemRequestService, times(1)).getAll(anyLong(), anyInt(), anyInt());
    }

    @Test
    void getAllPaginatioinErrorTest() throws Exception {
        mockMvc.perform(get("/requests/all?from=-1&size=0")
                        .header("X-Sharer-User-Id", uid)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemRequestService, never()).getAll(anyLong(), anyInt(), anyInt());
    }

    @Test
    void getAllTest() throws Exception {
        when(itemRequestService.getAll(anyLong(), anyInt(), anyInt())).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/requests/all?from=0&size=20")
                        .header("X-Sharer-User-Id", uid)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestDto.getDescription())));

        verify(itemRequestService, times(1)).getAll(anyLong(), anyInt(), anyInt());
    }
}
