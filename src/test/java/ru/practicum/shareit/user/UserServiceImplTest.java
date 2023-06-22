package ru.practicum.shareit.user;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Spy
    private ModelMapper modelMapper;
    @InjectMocks
    private UserServiceImpl userService;

    private static User user;
    private static UserDto userDto;

    @BeforeEach
    void init() {
        userDto = new UserDto(-1L, "user1", "user1@mail.com");
        user = modelMapper.map(userDto, User.class);
        user.setId(1L);
    }

    @Test
    @Order(1)
    void createUserTest() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto userDto2 = userService.add(userDto);

        assertEquals(userDto2, modelMapper.map(user, UserDto.class));
        verify(userRepository, Mockito.times(1)).save(any(User.class));
    }

    @Test
    @Order(2)
    void getUserByCorrectIdTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        UserDto userDto = userService.get(user.getId());

        assertEquals(userDto, modelMapper.map(user, UserDto.class));
        verify(userRepository, Mockito.times(1)).findById(anyLong());
    }

    @Test
    @Order(3)
    void getUserByInvalidIdTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.get(100L));
        verify(userRepository, Mockito.times(1)).findById(anyLong());
    }


    @Test
    @Order(4)
    void updateUserTest() {
        UserDto userDto1 = new UserDto(user.getId(), "updated1", "updated1@mail.com");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocationOnMock -> {
            return invocationOnMock.getArgument(0, User.class);
        });

        UserDto userUpdate = userService.update(userDto1);

        assertEquals(userUpdate.getName(), userDto1.getName(),
                "Имя пользователя в хранилище должно смениться на updated1");
        assertEquals(userUpdate.getEmail(), userDto1.getEmail(),
                "Почтовый адрес в хранилище должен смениться на updated1@mail.com");

        verify(userRepository, Mockito.times(1)).findById(1L);
        verify(userRepository, Mockito.times(1)).save(any(User.class));
    }



    @Test
    @Order(5)
    void updateUserNameOnlyTest() {
        UserDto userDto1 = new UserDto(user.getId(), "updated1", null);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocationOnMock -> {
            return invocationOnMock.getArgument(0, User.class);
        });

        UserDto userUpdate = userService.update(userDto1);

        assertEquals(userUpdate.getName(), userDto1.getName(),
                "Имя пользователя в хранилище должно смениться на updated1");
        assertEquals(userUpdate.getEmail(), user.getEmail(),
                "Почтовый адрес в хранилище не должен смениться");

        verify(userRepository, Mockito.times(1)).findById(1L);
        verify(userRepository, Mockito.times(1)).save(any(User.class));
    }

    @Test
    @Order(6)
    void updateUserEmailOnlyTest() {
        UserDto userDto1 = new UserDto(user.getId(), null, "name1@mail.com");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocationOnMock -> {
            return invocationOnMock.getArgument(0, User.class);
        });

        UserDto userUpdate = userService.update(userDto1);

        assertEquals(userUpdate.getName(), user.getName(),
                "Имя пользователя в хранилище не должно смениться");
        assertEquals(userUpdate.getEmail(), userDto1.getEmail(),
                "Почтовый адрес в хранилище должен измениться");

        verify(userRepository, Mockito.times(1)).findById(1L);
        verify(userRepository, Mockito.times(1)).save(any(User.class));
    }


    @Test
    @Order(7)
    void updateUserWithInvalidIdTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.get(100L));
        verify(userRepository, Mockito.times(1)).findById(anyLong());
    }

    @Test
    @Order(8)
    void deleteUserTest() {
            userService.remove(1L);

            verify(userRepository, Mockito.times(1)).deleteById(1L);
    }

    @Test
    @Order(9)
    void getAllUsersTest() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        Collection<UserDto> users = userService.getAll();

        assertNotNull(users);
        assertEquals(1, users.size(), "Должен быть 1 пользователь");
    }

    @Test
    @Order(10)
    void getAllUsersEmptyListTest() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        Collection<UserDto> users = userService.getAll();

        assertNotNull(users);
        assertEquals(0, users.size(), "Должен быть пустой список пользователей");
    }

}
