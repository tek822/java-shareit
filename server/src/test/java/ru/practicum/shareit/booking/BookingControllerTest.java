package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItTestsConfiguration;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingSimpleDto;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@ContextConfiguration(classes = {ShareItTestsConfiguration.class})
@TestPropertySource(locations = "classpath:test.properties")
class BookingControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ModelMapper modelMapper;
    @MockBean
    private BookingServiceImpl bookingService;

    private User owner = new User(1L, "owner", "owner@email.com");
    private User booker = new User(2L, "booker", "booker@email.com");
    private UserDto bookerDto = new UserDto(booker.getId(), booker.getName(), booker.getEmail());
    private Item item = new Item(1L, "item1", "item1 description", true, owner, null);
    private ItemDto itemDto = new ItemDto(item.getId(), item.getName(), item.getDescription(),
            true, null, null, new ArrayList<CommentDto>(), null);
    private Booking booking = new Booking(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2),
            item, booker, BookingStatus.APPROVED);
    private BookingDto bookingDto = new BookingDto(booking.getId(), booking.getStart(), booking.getEnd(),
            itemDto, bookerDto, booking.getStatus().name());
    private BookingSimpleDto bookingSimpleDto = new BookingSimpleDto(booking.getId(), booking.getStart(), booking.getEnd(),
            booker.getId(), item.getId(), booking.getStatus().name());


    @Test
    void addBookingWithWrongUserTest() throws Exception {
        when(bookingService.add(anyLong(), any())).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", booker.getId())
                        .content(mapper.writeValueAsString(bookingSimpleDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(bookingService, times(1)).add(anyLong(), any());
    }

    @Test
    void addBookingWithWrongItemTest() throws Exception {
        when(bookingService.add(anyLong(), any())).thenThrow(new ItemNotFoundException("Item not found"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", booker.getId())
                        .content(mapper.writeValueAsString(bookingSimpleDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(bookingService, times(1)).add(anyLong(), any());
    }


    @Test
    void addBookingByItemOwnerTest() throws Exception {
        when(bookingService.add(anyLong(), any())).thenThrow(new BookingNotFoundException("Owner can not create booking"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", owner.getId())
                        .content(mapper.writeValueAsString(bookingSimpleDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(bookingService, times(1)).add(anyLong(), any());
    }

    @Test
    void addBookingTest() throws Exception {
        when(bookingService.add(anyLong(), any())).thenReturn(bookingDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", booker.getId())
                        .content(mapper.writeValueAsString(bookingSimpleDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(booker.getId()), Long.class));

        verify(bookingService, times(1)).add(anyLong(), any());
    }

    @Test
    void approveBookingTest() throws Exception {
        when(bookingService.approve(booking.getId(), owner.getId(), false)).thenReturn(bookingDto);

        mockMvc.perform(patch("/bookings/1?approved=false")
                        .header("X-Sharer-User-Id", owner.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(booker.getId()), Long.class))
                .andExpect(jsonPath("$.status", is("APPROVED")));

        verify(bookingService, times(1)).approve(booking.getId(), owner.getId(), false);
    }

    @Test
    void getBookingTest() throws Exception {
        when(bookingService.get(booking.getId(), booker.getId())).thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", booker.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(booker.getId()), Long.class))
                .andExpect(jsonPath("$.status", is("APPROVED")));

        verify(bookingService, times(1)).get(booking.getId(), booker.getId());
    }

    @Test
    void getBookingsTest() throws Exception {
        int from = 0;
        int size = 20;
        String state = "APPROVED";
        when(bookingService.getOwnBookings(booker.getId(), state, from, size)).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/?state=APPROVED&from=0&size=20")
                        .header("X-Sharer-User-Id", booker.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.id", is(booker.getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is("APPROVED")));

        verify(bookingService, times(1)).getOwnBookings(booker.getId(), state, from, size);
    }

    @Test
    void getOwnerBookingsTest() throws Exception {
        int from = 0;
        int size = 20;
        String state = "APPROVED";
        when(bookingService.getBookingsForOwnItems(owner.getId(), state, from, size)).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner?state=APPROVED&from=0&size=20")
                        .header("X-Sharer-User-Id", owner.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.id", is(booker.getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is("APPROVED")));

        verify(bookingService, times(1)).getBookingsForOwnItems(owner.getId(), state, from, size);
    }
}

