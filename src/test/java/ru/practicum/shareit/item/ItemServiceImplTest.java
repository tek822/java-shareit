package ru.practicum.shareit.item;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.practicum.shareit.ShareItTestsConfiguration;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.CommentBadRequestException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.UpdateForbiddenException;
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

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ContextConfiguration(classes = {ShareItTestsConfiguration.class})
@ExtendWith({MockitoExtension.class, SpringExtension.class})
class ItemServiceImplTest {
    @Spy
    @Autowired
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
        notOwner.setId(user.getId() + 1);
        Booking bookingInPast = new Booking(
                1L,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().minusMinutes(10),
                item,
                notOwner,
                BookingStatus.APPROVED);
        Booking bookingInFuture = new Booking(
                2L,
                LocalDateTime.now().plusMinutes(10),
                LocalDateTime.now().minusHours(1),
                item,
                notOwner,
                BookingStatus.APPROVED);
        // db implements booking sorting by start.DateTime
        when(bookingRepository.findAllByItemId(anyLong())).thenReturn(List.of(bookingInPast, bookingInFuture));
        when(commentRepository.findAllByItemId(anyLong())).thenReturn(List.of());
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        ItemDto returnedItemDto = itemService.get(item.getId(), user.getId());

        assertNull(returnedItemDto.getRequestId());
        assertNotNull(returnedItemDto.getNextBooking());
        assertEquals(2L, returnedItemDto.getNextBooking().getId());
        assertEquals(1L, returnedItemDto.getLastBooking().getId());
        assertNotNull(returnedItemDto.getComments());
        assertEquals(0, returnedItemDto.getComments().size());

        verify(bookingRepository, Mockito.times(1)).findAllByItemId(anyLong());
        verify(itemRepository, Mockito.times(1)).findById(anyLong());
        verify(commentRepository, Mockito.times(1)).findAllByItemId(anyLong());
    }

    @Test
    @Order(6)
    void getItemNotForOwnerTest() {
        notOwner = new User(user);
        notOwner.setId(user.getId() + 1);

        when(commentRepository.findAllByItemId(anyLong())).thenReturn(List.of());
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        ItemDto returnedItemDto = itemService.get(item.getId(), notOwner.getId());

        assertNull(returnedItemDto.getRequestId());
        assertNull(returnedItemDto.getNextBooking());
        assertNull(returnedItemDto.getLastBooking());
        assertNotNull(returnedItemDto.getComments());
        assertEquals(0, returnedItemDto.getComments().size());

        verify(bookingRepository, Mockito.never()).findAllByItemId(anyLong());
        verify(commentRepository, Mockito.times(1)).findAllByItemId(anyLong());
        verify(itemRepository, Mockito.times(1)).findById(anyLong());
    }

    @Test
    @Order(7)
    void getAllItemsForWrongUserIdTest() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> itemService.getAll(-1L, 0, 20));

        verify(itemRepository, never()).findAllByUserId(anyLong());
    }

    @Test
    @Order(8)
    void getAllItemsForUserTest() {
        Item item2 = new Item(2L, "Item2 name", "Item2 description", true, user, null);
        LocalDateTime now = LocalDateTime.now();
        int from = 0;
        int size = 20;

        PageRequest page = PageRequest.of(from / size, size);
        Booking booking1 = new Booking(1L, now.minusHours(1), now.minusMinutes(10), item, user, BookingStatus.APPROVED);
        Booking booking2 = new Booking(2L, now.plusMinutes(10), now.plusHours(1),  item2, user, BookingStatus.APPROVED);
        Comment comment1 = new Comment(11L, "comment 1 on item 1", item, user, LocalDateTime.now());
        Comment comment2 = new Comment(22L, "comment 2 on item 2", item2, user, LocalDateTime.now());

        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(itemRepository.findAllByUserId(user.getId(), page)).thenReturn(List.of(item, item2));
        when(bookingRepository.findAllByItemIdIn(any())).thenReturn(List.of(booking1, booking2));
        when(commentRepository.findAllByItemIdIn(any())).thenReturn(List.of(comment1, comment2));

        ItemDto[] itemDtos = itemService.getAll(user.getId(), from, size).toArray(new ItemDto[2]);
        assertEquals(2L, itemDtos.length);
        ItemDto itemDto1 = itemDtos[0];
        ItemDto itemDto2 = itemDtos[1];

        assertEquals(1L, itemDto1.getId());
        assertNotNull(itemDto1.getLastBooking());
        assertTrue(itemDto1.getLastBooking().getStart().isBefore(now));
        assertNull(itemDto1.getNextBooking());
        assertNotNull(itemDto1.getComments());
        assertEquals(1L, itemDto1.getComments().size());
        assertEquals(11L, itemDto1.getComments().get(0).getId());

        assertEquals(2L, itemDto2.getId());
        assertNotNull(itemDto2.getNextBooking());
        assertTrue(itemDto2.getNextBooking().getStart().isAfter(now));
        assertNull(itemDto2.getLastBooking());
        assertNotNull(itemDto2.getComments());
        assertEquals(1L, itemDto2.getComments().size());
        assertEquals(22L, itemDto2.getComments().get(0).getId());


        verify(itemRepository, times(1)).findAllByUserId(user.getId(), page);
        verify(bookingRepository, times(1)).findAllByItemIdIn(any());
        verify(commentRepository, times(1)).findAllByItemIdIn(any());
    }

    @Test
    @Order(9)
    void updateItemWithWrongUserIdTest() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> itemService.update(user.getId(), itemDto));

        verify(itemRepository, never()).save(any());
    }

    @Test
    @Order(10)
    void updateItemWithWrongItemIdTest() {
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(itemRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> itemService.update(user.getId(), itemDto));

        verify(itemRepository, never()).save(any());
    }

    @Test
    @Order(11)
    void updateItemWithNotOwnerIdTest() {
        notOwner = new User(user);
        notOwner.setId(user.getId() + 1);

        when(userRepository.findById(any())).thenReturn(Optional.of(notOwner));
        when(itemRepository.findById(any())).thenReturn(Optional.of(item));

        assertThrows(UpdateForbiddenException.class, () -> itemService.update(notOwner.getId(), itemDto));

        verify(itemRepository, never()).save(any());
    }

    @Test
    @Order(12)
    void updateItemTest() {
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(itemRepository.findById(any())).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenAnswer(invocationOnMock -> {
            return invocationOnMock.getArgument(0, Item.class);
        });

        ItemDto update = itemService.update(item.getOwner().getId(), itemDto);

        assertEquals(itemDto, update);

        verify(userRepository, times(1)).findById(any());
        verify(itemRepository, times(1)).findById(any());
        verify(itemRepository, times(1)).save(any());
    }

    @Test
    @Order(13)
    void findAvailableNullAndEmptyStrigTest() {
        Collection<ItemDto> items = itemService.findAvailable(null, 0, 20);
        assertNotNull(items);
        assertEquals(0L, items.size());

        items = itemService.findAvailable(null, 0, 20);
        assertNotNull(items);
        assertEquals(0L, items.size());
    }

    @Test
    @Order(14)
    void findAvailableTest() {
        String text = "test";
        int from = 0;
        int size = 20;
        PageRequest page = PageRequest.of(from / size, size);

        item.setDescription("available for test");
        Item item2 = new Item(item);
        item2.setId(2);
        item2.setAvailable(false);

        when(itemRepository.findAllByDescriptionContainingIgnoreCaseOrNameContainingIgnoreCase(text, text, page))
                .thenReturn(List.of(item, item2));

        Collection<ItemDto> itemDtos = itemService.findAvailable(text, from, size);

        assertEquals(1L, itemDtos.size());
        assertEquals(1L, itemDtos.iterator().next().getId());

        verify(itemRepository, times(1)).findAllByDescriptionContainingIgnoreCaseOrNameContainingIgnoreCase(text, text, page);
    }

    @Test
    @Order(15)
    void addCommentFailureTest() {
        User booker = new User(2L, "booker", "booker@mail.com");
        LocalDateTime now = LocalDateTime.now();
        Booking booking = new Booking(11L, now.minusHours(1), now.minusMinutes(10), item, booker, BookingStatus.REJECTED);
        CommentDto commentDto = new CommentDto(111L, "Comment from booker", item.getId(), booker.getName(), now);

        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(itemRepository.findById(any())).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByBooker(booker.getId())).thenReturn(List.of(booking));

        assertThrows(CommentBadRequestException.class, () -> itemService.addComment(item.getId(), booker.getId(), commentDto));

        verify(userRepository, times(1)).findById(any());
        verify(itemRepository, times(1)).findById(any());
        verify(bookingRepository, times(1)).findAllByBooker(booker.getId());
    }

    @Test
    @Order(15)
    void addCommentTest() {
        User booker = new User(2L, "booker", "booker@mail.com");
        LocalDateTime now = LocalDateTime.now();
        Booking booking = new Booking(11L, now.minusHours(1), now.minusMinutes(10), item, booker, BookingStatus.APPROVED);
        Comment comment = new Comment(111L, "Comment from booker", item, booker, now);
        CommentDto commentDto = modelMapper.map(comment, CommentDto.class);

        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(itemRepository.findById(any())).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByBooker(booker.getId())).thenReturn(List.of(booking));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto reply = itemService.addComment(item.getId(), booker.getId(), commentDto);
        assertEquals(reply, commentDto);


        verify(userRepository, times(1)).findById(any());
        verify(itemRepository, times(1)).findById(any());
        verify(bookingRepository, times(1)).findAllByBooker(booker.getId());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }
}


