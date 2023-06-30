package ru.practicum.shareit.request;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.exception.ItemRequestNotFoundException;
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
@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Spy
    private ModelMapper modelMapper;
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private static User requestor;
    private static User owner;
    private static ItemRequestDto itemRequestDto;
    private static ItemRequest itemRequest;

    @BeforeAll
    static void init() {
        owner = new User(1L, "Owner", "owner@mail.com");
        requestor = new User(2L, "ReQuEsToR", "requestor@mail.com");
        itemRequestDto = new ItemRequestDto(null, "request description", null, null);
        itemRequest = new ItemRequest(1L, "ItemRequest from requestor", requestor, LocalDateTime.now());
    }

    @Test
    @Order(1)
    void addItemRequestWithWrongUserIdTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> itemRequestService.add(-1L, itemRequestDto));
        verify(itemRequestRepository, never()).save(any(ItemRequest.class));
    }

    @Test
    @Order(2)
    void  getOwnWithWrongUserIdTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> itemRequestService.add(-1L, itemRequestDto));
        verify(itemRequestRepository, never()).findById(anyLong());
    }

    @Test
    @Order(3)
    void getOwnWithoutItemsTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requestor));
        when(itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(anyLong())).thenReturn(List.of(itemRequest));
        when(itemRepository.findAllByRequestIdIn(any())).thenReturn(List.of());

        Collection<ItemRequestDto> itemRequestDtos = itemRequestService.getOwn(requestor.getId());

        assertEquals(1L, itemRequestDtos.size());
        assertTrue(itemRequestDtos.contains(modelMapper.map(itemRequest, ItemRequestDto.class)));

        verify(userRepository, Mockito.times(1)).findById(any());
        verify(itemRequestRepository, Mockito.times(1)).findAllByRequestorIdOrderByCreatedDesc(anyLong());
        verify(itemRepository, Mockito.times(1)).findAllByRequestIdIn(any());
    }

    @Test
    @Order(4)
    void getOwnWithItemsTest() {
        Item item = new Item(1L, "Item1", "ItemForRequest", true, owner, itemRequest);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requestor));
        when(itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(anyLong())).thenReturn(List.of(itemRequest));
        when(itemRepository.findAllByRequestIdIn(any())).thenReturn(List.of(item));

        Collection<ItemRequestDto> itemRequestDtos = itemRequestService.getOwn(requestor.getId());
        ItemRequestDto itemRequestDto = itemRequestDtos.iterator().next();

        assertEquals(1L, itemRequestDtos.size());
        assertEquals(1L, itemRequestDto.getItems().size());
        assertEquals(itemRequestDto.getItems().get(0), modelMapper.map(item, ItemDto.class));

        verify(userRepository, Mockito.times(1)).findById(any());
        verify(itemRequestRepository, Mockito.times(1)).findAllByRequestorIdOrderByCreatedDesc(anyLong());
        verify(itemRepository, Mockito.times(1)).findAllByRequestIdIn(any());
    }

    @Test
    @Order(5)
    void addNewItemRequestTest() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(requestor));
        when(itemRequestRepository.save(any(ItemRequest.class)))
                .thenReturn(new ItemRequest(1L, "desc", requestor, LocalDateTime.now()));

        ItemRequestDto returnValue = itemRequestService.add(requestor.getId(), itemRequestDto);

        assertNotNull(returnValue);
        assertEquals(1L, returnValue.getId(), "Request Id должен быть 1");
        assertEquals("desc", returnValue.getDescription(), "Request Description должен быть 'desc'");
        assertNotNull(returnValue.getCreated());
        assertNotNull(returnValue.getItems());

        verify(userRepository, Mockito.times(1)).findById(requestor.getId());
        verify(itemRequestRepository, Mockito.times(1)).save(any(ItemRequest.class));
    }

    @Test
    @Order(6)
    void getAllWithWrongUidTest() {
        int from = 0;
        int size = 20;
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> itemRequestService.getAll(-1L, from, size));

        verify(itemRequestRepository, never()).findAllByRequestorIdNotOrderByCreatedDesc(-1L, PageRequest.of(from / size, size));
    }

    @Test
    @Order(7)
    void getAllTest() {
        int from = 0;
        int size = 20;
        PageRequest page = PageRequest.of(from / size, size);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(owner.getId(), page)).thenReturn(List.of(itemRequest));

        Collection<ItemRequestDto> requestDtos = itemRequestService.getAll(owner.getId(), from, size);

        assertNotNull(requestDtos);
        assertEquals(1L, requestDtos.size());
        assertEquals(modelMapper.map(itemRequest, ItemRequestDto.class), requestDtos.iterator().next());

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).findAllByRequestorIdNotOrderByCreatedDesc(owner.getId(), page);
    }

    @Test
    @Order(8)
    void getWithWrongUidTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> itemRequestService.get(requestor.getId(), itemRequest.getId()));

        verify(itemRequestRepository, never()).findById(anyLong());
    }

    @Test
    @Order(9)
    void getTest() {
        int from = 0;
        int size = 20;
        PageRequest page = PageRequest.of(from / size, size);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(itemRequest));

        ItemRequestDto requestDto = itemRequestService.get(owner.getId(), itemRequest.getId());

        assertNotNull(requestDto);
        assertEquals(modelMapper.map(itemRequest, ItemRequestDto.class), requestDto);

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).findById(anyLong());
    }

    @Test
    @Order(10)
    void getWrongItemRequestIdTest() {
        int from = 0;
        int size = 20;
        PageRequest page = PageRequest.of(from / size, size);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ItemRequestNotFoundException.class, () -> itemRequestService.get(owner.getId(), itemRequest.getId()));
    }
}
