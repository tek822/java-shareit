package ru.practicum.shareit;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingDto;
import ru.practicum.shareit.booking.BookingSimpleDto;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.user.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BookingClient bookingClient;

    private LocalDateTime now = LocalDateTime.now();
    private ItemDto item = new ItemDto(1L, "item", "description", true, null, null, null, null);
    private UserDto owner = new UserDto(1L, "owner", "owner@email.com");
    private UserDto booker = new UserDto(2L, "booker", "booker@email.com");
    private BookingSimpleDto bookingSimpleDto =
            new BookingSimpleDto(0L, now.plusHours(1), now.plusHours(2), booker.getId(), item.getId(), "APPROVED");
    private BookingDto bookingDto = new BookingDto(
            bookingSimpleDto.getId(),
            bookingSimpleDto.getStart(),
            bookingSimpleDto.getEnd(),
            item, booker, "APPROVED");


    @Test
    void addBookingWithNullItemIdTest() throws Exception {
        BookingSimpleDto bookingSimpleDto = new BookingSimpleDto(0L, now.plusHours(1), now.plusHours(1), 0L, null, "");

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 0L)
                        .content(mapper.writeValueAsString(bookingSimpleDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).addBooking(anyLong(), any());
    }

    @Test
    void addBookingWithNullStartTest() throws Exception {
        BookingSimpleDto bookingSimpleDto = new BookingSimpleDto(0L, null, now.plusHours(1), 0L, 0L, "");

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 0L)
                        .content(mapper.writeValueAsString(bookingSimpleDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).addBooking(anyLong(), any());
    }

    @Test
    void addBookingWithNullEndTest() throws Exception {

        BookingSimpleDto bookingSimpleDto = new BookingSimpleDto(0L, now.plusHours(1), null, 0L, 0L, "");

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 0L)
                        .content(mapper.writeValueAsString(bookingSimpleDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).addBooking(anyLong(), any());
    }

    @Test
    void addBookingWithStartEqualsEndTest() throws Exception {
        BookingSimpleDto bookingSimpleDto = new BookingSimpleDto(0L, now.plusHours(1), now.plusHours(1), 0L, 0L, "");

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 0L)
                        .content(mapper.writeValueAsString(bookingSimpleDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).addBooking(anyLong(), any());
    }

    @Test
    void addBookingWithStartAfterEndTest() throws Exception {
        BookingSimpleDto bookingSimpleDto = new BookingSimpleDto(0L, now.plusHours(2), now.plusHours(1), 0L, 0L, "");

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 0L)
                        .content(mapper.writeValueAsString(bookingSimpleDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).addBooking(anyLong(), any());
    }

    @Test
    void addBookingWithStartInPastTest() throws Exception {
        BookingSimpleDto bookingSimpleDto = new BookingSimpleDto(0L, now.minusHours(1), now.plusHours(1), 0L, 0L, "");

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 0L)
                        .content(mapper.writeValueAsString(bookingSimpleDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).addBooking(anyLong(), any());
    }

    @Test
    void addBookingTest() throws Exception {
        when(bookingClient.addBooking(anyLong(), any())).thenReturn(ResponseEntity.ok(bookingDto));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", booker.getId())
                        .content(mapper.writeValueAsString(bookingSimpleDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(bookingDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(booker.getId()), Long.class));

        verify(bookingClient, times(1)).addBooking(anyLong(), any());
    }

    @Test
    void approveBookingValidationTest() throws Exception {
        mockMvc.perform(patch("/bookings/1?approved=df")
                        .header("X-Sharer-User-Id", owner.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).addBooking(anyLong(), any());
    }
}