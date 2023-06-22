package ru.practicum.shareit.item;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.awt.print.Book;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @Spy
    private ModelMapper modelMapper;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @InjectMocks
    private ItemServiceImpl itemService;

    private Item item;
    private ItemDto itemDto;
    private User user;
    private User notOwner;
    private UserDto userDto;
    private Comment comment;
    private CommentDto commentDto;
    private ItemRequestDto itemRequestDto;
    private ItemRequest itemRequest;

    @BeforeEach
    void init() {
        user = new User(1L, "User1", "user1@mail.com");
        item = new Item(1L, "Item1", "Item1 description", true, user, null);
        itemDto = modelMapper.map(item, ItemDto.class);
        itemRequest = new ItemRequest(1L, "ItemRequest1 description", user, LocalDateTime.now());
    }

    @Test
    @Order(1)
    void addItemWithWrongUserIdTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> itemService.add(-1L, itemDto));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    @Order(2)
    void addItemWithoutRequestIdTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocationOnMock -> {
            Item item = invocationOnMock.getArgument(0, Item.class);
            item.setId(1L);
            return item;
        });

        ItemDto returnedItemDto = itemService.add(user.getId(), itemDto);

        assertNull(returnedItemDto.getRequestId());
        assertEquals(itemDto.getRequestId(), returnedItemDto.getRequestId());
        assertEquals(itemDto, returnedItemDto);

        verify(itemRepository, Mockito.times(1)).save(any(Item.class));
        verify(itemRequestRepository, never()).findById(anyLong());
        verify(userRepository, Mockito.times(1)).findById(anyLong());
    }

    @Test
    @Order(3)
    void addItemWithRequestIdTest() {
        itemDto.setRequestId(111L);

        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(itemRequest));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocationOnMock -> {
            Item item = invocationOnMock.getArgument(0, Item.class);
            item.setId(1L);
            return item;
        });

        ItemDto returnedItemDto = itemService.add(user.getId(), itemDto);

        assertNotNull(returnedItemDto.getRequestId());
        assertEquals(itemDto, returnedItemDto);

        verify(itemRepository, Mockito.times(1)).save(any(Item.class));
        verify(itemRequestRepository, Mockito.times(1)).findById(anyLong());
        verify(userRepository, Mockito.times(1)).findById(anyLong());
    }

    @Test
    @Order(4)
    void getItemWithWrongIdTest() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> {
            itemService.get(itemDto.getId(), user.getId());
        });

        verify(itemRepository, Mockito.times(1)).findById(anyLong());
    }

    @Test
    @Order(5)
    void getItemForOwnerTest() {
        notOwner = new User(user);
        notOwner.setId(2L);
        Booking bookingPast = new Booking(
                1L,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().minusMinutes(10),
                item,
                notOwner,
                BookingStatus.APPROVED);
        Booking bookingFuture = new Booking(
                1L,
                LocalDateTime.now().plusMinutes(10),
                LocalDateTime.now().minusHours(1),
                item,
                notOwner,
                BookingStatus.APPROVED);

        when(bookingRepository.findAllByItemId(anyLong())).thenReturn(
                List.of(bookingFuture, bookingPast)
        );
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(itemRequest));
        when(commentRepository.findAllByItemId(anyLong())).thenReturn(List.of());
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        ItemDto returnedItemDto = itemService.get(item.getId(), user.getId());

        assertNotNull(returnedItemDto.getRequestId());
        assertEquals(itemDto, returnedItemDto);

        verify(bookingRepository, Mockito.times(1)).findAllByItemIdIn(anyList());
        verify(itemRequestRepository, Mockito.times(1)).findById(anyLong());
        verify(commentRepository, Mockito.times(1)).findAllByItemId(anyLong());
    }
}


