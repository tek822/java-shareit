package ru.practicum.shareit.booking;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingSimpleDto;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "db.name=test", webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test.properties")
class BookingServiceIntegrationTest {
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private BookingServiceImpl bookingService;

    private User owner = new User(0L, "owner", "owner@email.com");
    private UserDto ownerDto;
    private User booker = new User(0L, "booker", "booker@email.com");
    private UserDto bookerDto;
    private Item item1 = new Item(0L, "item1", "item1 description", true, owner, null);
    private Item item2 = new Item(0L, "item2", "item2 description", true, owner, null);
    private ItemDto itemDto;
    private Booking booking1 = new Booking(0L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2),
            item1, booker, BookingStatus.APPROVED);
    private Booking booking2 = new Booking(0L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2),
            item2, booker, BookingStatus.APPROVED);

    private final int from = 0;
    private final int size = 20;

    @BeforeEach
    void initDb() {
        owner = userRepository.save(owner);
        booker = userRepository.save(booker);
        item1.setOwner(owner);
        item1 = itemRepository.save(item1);
        item2.setOwner(owner);
        item2 = itemRepository.save(item2);
        booking1.setItem(item1);
        booking2.setItem(item2);
    }

    @AfterEach
    void clearDb() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void getAllBookingsByBookerTest() {
        BookingDto bookingDto1 = bookingService.add(booker.getId(), modelMapper.map(booking1, BookingSimpleDto.class));
        BookingDto bookingDto2 = bookingService.add(booker.getId(), modelMapper.map(booking2, BookingSimpleDto.class));

        List<BookingDto> bookings = bookingService.getOwnBookings(booker.getId(), "ALL", from, size);

        assertEquals(2L, bookings.size());
        assertTrue(bookings.containsAll(List.of(bookingDto1, bookingDto2)));
    }

    @Test
    void getAllBookingsForOwnerItems() {
        BookingDto bookingDto1 = bookingService.add(booker.getId(), modelMapper.map(booking1, BookingSimpleDto.class));
        BookingDto bookingDto2 = bookingService.add(booker.getId(), modelMapper.map(booking2, BookingSimpleDto.class));

        List<BookingDto> bookings = bookingService.getBookingsForOwnItems(owner.getId(), "ALL", from, size);

        assertEquals(2L, bookings.size());
        assertTrue(bookings.containsAll(List.of(bookingDto1, bookingDto2)));
    }
}
