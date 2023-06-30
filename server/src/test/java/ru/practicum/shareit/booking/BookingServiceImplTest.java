package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.practicum.shareit.ShareItTestsConfiguration;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingSimpleDto;
import ru.practicum.shareit.booking.exception.BookingBadRequestException;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ContextConfiguration(classes = {ShareItTestsConfiguration.class})
@ExtendWith({MockitoExtension.class, SpringExtension.class})

class BookingServiceImplTest {
    @Spy
    @Autowired
    private ModelMapper modelMapper;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @InjectMocks
    private BookingServiceImpl bookingService;

    private User owner = new User(1L, "owner", "owner@email.com");
    private UserDto ownerDto;
    private User booker = new User(2L, "booker", "booker@email.com");
    private UserDto bookerDto;
    private Item item = new Item(1L, "item1", "item1 description", true, owner, null);
    private ItemDto itemDto;
    private Booking booking = new Booking(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2),
            item, booker, BookingStatus.APPROVED);

    private BookingDto bookingDto;
    private BookingSimpleDto bookingSimpleDto;

    private final int from = 0;
    private final int size = 20;
    private final PageRequest page = PageRequest.of(from / size, size);


    @BeforeEach
    void init() {
        item.setAvailable(true);
        ownerDto = modelMapper.map(owner, UserDto.class);
        bookerDto = modelMapper.map(booker, UserDto.class);
        itemDto = modelMapper.map(item, ItemDto.class);
        bookingDto = modelMapper.map(booking, BookingDto.class);
        bookingSimpleDto = modelMapper.map(booking, BookingSimpleDto.class);
    }

    @Test
    void bookingAddInvalidUserTest() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> bookingService.add(booker.getId(), bookingSimpleDto));
    }

    @Test
    void bookingAddInvalidItemTest() {
        when(userRepository.findById(any())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> bookingService.add(booker.getId(), bookingSimpleDto));
    }

    @Test
    void bookingAddItemUnavailableTest() {
        item.setAvailable(false);
        when(userRepository.findById(any())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(any())).thenReturn(Optional.of(item));

        assertThrows(BookingBadRequestException.class, () -> bookingService.add(booker.getId(), bookingSimpleDto));
    }

    @Test
    void bookingAddByItemOwnerTest() {
        when(userRepository.findById(any())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(any())).thenReturn(Optional.of(item));

        assertThrows(BookingNotFoundException.class, () -> bookingService.add(owner.getId(), bookingSimpleDto));
    }

    @Test
    void bookingAddTest() {
        when(userRepository.findById(any())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(any())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0, Booking.class));

        BookingDto reply = bookingService.add(booker.getId(), bookingSimpleDto);
        booking.setStatus(BookingStatus.WAITING);

        assertEquals(modelMapper.map(booking, BookingDto.class), reply);

        verify(bookingRepository, times(1)).save(any());
        booking.setStatus(BookingStatus.APPROVED);
    }

    @Test
    void approveBookingWithWrongUidTest() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> bookingService.add(booker.getId(), bookingSimpleDto));
    }

    @Test
    void approveBookingWithWrongBookingIdTest() {
        when(userRepository.findById(any())).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class, () -> bookingService.approve(booking.getId(), owner.getId(), false));
    }

    @Test
    void approveBookingWithWrongItemOwnerIdTest() {
        when(userRepository.findById(any())).thenReturn(Optional.of(booker));
        when(bookingRepository.findById(any())).thenReturn(Optional.of(booking));

        assertThrows(BookingNotFoundException.class, () -> bookingService.approve(booking.getId(), booker.getId(), false));
    }

    @Test
    void approveBookingWithWrongStatusTest() {
        when(userRepository.findById(any())).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(any())).thenReturn(Optional.of(booking));

        assertThrows(BookingBadRequestException.class, () -> bookingService.approve(booking.getId(), owner.getId(), false));
    }

    @Test
    void approveBookingTest() {
        booking.setStatus(BookingStatus.WAITING);
        when(userRepository.findById(any())).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(any())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0, Booking.class));

        BookingDto reply = bookingService.approve(booking.getId(), owner.getId(), true);

        assertEquals(BookingStatus.APPROVED.name(), reply.getStatus());

        booking.setStatus(BookingStatus.APPROVED);
        assertEquals(modelMapper.map(booking, BookingDto.class), reply);
    }

    @Test
    void rejectBookingTest() {
        booking.setStatus(BookingStatus.WAITING);
        when(userRepository.findById(any())).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(any())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0, Booking.class));

        BookingDto reply = bookingService.approve(booking.getId(), owner.getId(), false);

        assertEquals(BookingStatus.REJECTED.name(), reply.getStatus());

        booking.setStatus(BookingStatus.REJECTED);
        assertEquals(modelMapper.map(booking, BookingDto.class), reply);

        booking.setStatus(BookingStatus.APPROVED);
    }

    @Test
    void getBookingWithInvalidBookingIdTest() {
        when(bookingRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class, () -> bookingService.get(booking.getId(), owner.getId()));
    }

    @Test
    void getBookingWithInvalidUidTest() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        when(bookingRepository.findById(any())).thenReturn(Optional.of(booking));

        assertThrows(UserNotFoundException.class, () -> bookingService.get(booking.getId(), owner.getId()));
    }

    @Test
    void getBookingForbiddenTest() {
        User user = new User(3L, "bystander", "mail@mail.com");
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(bookingRepository.findById(any())).thenReturn(Optional.of(booking));

        assertThrows(BookingNotFoundException.class, () -> bookingService.get(booking.getId(), user.getId()));
    }

    @Test
    void getBookingTest() {
        when(userRepository.findById(any())).thenReturn(Optional.of(booker));
        when(bookingRepository.findById(any())).thenReturn(Optional.of(booking));

        BookingDto reply = bookingService.get(booking.getId(), booker.getId());

        assertEquals(modelMapper.map(booking, BookingDto.class), reply);
    }

    @Test
    void getOwnBookingsWithWrongUidTest() {
        String state = "ALL";
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> bookingService.getOwnBookings(booker.getId(), state, from, size));
    }

    @Test
    void getOwnBookingsAllTest() {
        String state = "ALL";
        when(userRepository.findById(any())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBooker(booker.getId(), page)).thenReturn(List.of(booking));

        List<BookingDto> reply = bookingService.getOwnBookings(booker.getId(), state, from, size);

        assertEquals(1L, reply.size());
        assertTrue(reply.contains(modelMapper.map(booking, BookingDto.class)));
    }

    @Test
    void getBookingsForOwnItemsTest() {
        String state = "ALL";
        when(userRepository.findById(any())).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByOwner(owner.getId(), page)).thenReturn(List.of(booking));

        List<BookingDto> reply = bookingService.getBookingsForOwnItems(owner.getId(), state, from, size);

        assertEquals(1L, reply.size());
        assertTrue(reply.contains(modelMapper.map(booking, BookingDto.class)));
    }
}


