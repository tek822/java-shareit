package ru.practicum.shareit.booking;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@DataJpaTest
@Transactional
class BookingJpaTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookingRepository bookingRepository;

    private User owner = new User(0L, "owner", "owner@email.com");
    private User booker = new User(0L, "booker", "booker@email.com");
    private Item item1 = new Item(0L, "item1", "item1 description", true, owner, null);
    private Item item2 = new Item(0L, "item2", "item2 description", true, owner, null);
    private Booking booking1 = new Booking(0L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2),
            item1, booker, BookingStatus.APPROVED);
    private Booking booking2 = new Booking(0L, LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(3),
            item2, booker, BookingStatus.APPROVED);

    private final int from = 0;
    private final int size = 20;
    private final PageRequest page = PageRequest.of(from / size, size);

    @BeforeEach
    void initDb() {
        owner = userRepository.save(owner);
        booker = userRepository.save(booker);
        item1.setOwner(owner);
        item1 = itemRepository.save(item1);
        item2.setOwner(owner);
        item2 = itemRepository.save(item2);
        booking1.setItem(item1);
        booking1 = bookingRepository.save(booking1);
        booking2.setItem(item2);
        booking2 = bookingRepository.save(booking2);
    }

    @AfterEach
    void clearDb() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findAllFutureByBookerTest() {
        List<Booking> bookings = bookingRepository.findAllFutureByBooker(booker.getId(), page);

        assertTrue(bookings.containsAll(List.of(booking1, booking2)));
    }

    @Test
    void findAllPastByBookerTest() {
        List<Booking> bookings = bookingRepository.findAllPastByBooker(booker.getId(), page);

        assertEquals(0L, bookings.size());
    }

    @Test
    void findAllFutureByOwner() {
        List<Booking> bookings = bookingRepository.findAllFutureByOwner(owner.getId(), page);

        assertTrue(bookings.containsAll(List.of(booking1, booking2)));
    }

    @Test
    void findAllPastByOwner() {
        List<Booking> bookings = bookingRepository.findAllPastByOwner(owner.getId(), page);

        assertEquals(0L, bookings.size());
    }

    @Test
    void findAllByItemId() {
        List<Booking> bookings = bookingRepository.findAllByItemId(item1.getId());

        assertEquals(booking1, bookings.get(0));
    }

    @Test
    void findAllByItemIdId() {
        List<Booking> bookings = bookingRepository.findAllByItemIdIn(List.of(item1.getId(), item2.getId()));

        assertTrue(bookings.containsAll(List.of(booking1, booking2)));
    }

}
