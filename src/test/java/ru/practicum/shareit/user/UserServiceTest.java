package ru.practicum.shareit.user;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.UserEmailAlreadyExists;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class UserServiceTest {
    final UserService userService;
    final UserRepository userRepository;
    private static long uid1;
    private static long uid2;
    private static long uid3;

    @Autowired
    public UserServiceTest(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Test
    @Order(1)
    void createFirstUserTest() {
        UserDto userDto1 = new UserDto(-1L, "user1", "user1@mail.com");
        UserDto userDto2 = userService.add(userDto1);

        uid1 = userDto2.getId();
        assertEquals(uid1, 1, "Пользователю должен был быть присвоен id: 1");

        User user1 = userRepository.get(uid1);
        assertEquals(user1.getName(), userDto1.getName(), "Имя в хранилище должно совпадать с переданным в dto");
        assertEquals(user1.getEmail(), userDto1.getEmail(), "email в хранилище должeн совпадать с переданным в dto");
    }

    @Test
    @Order(2)
    void createUserWithDuplicateEmailTest() {
        UserDto userDto = new UserDto(-1L, "user2", "user1@mail.com");

        assertThrows(UserEmailAlreadyExists.class, () -> userService.add(userDto),
                "Почтовый адрес уже используется");
    }

    @Test
    @Order(3)
    void updateUserTest() {
        UserDto userDto1 = new UserDto(uid1, "updated1", "updated1@mail.com");
        UserDto userDto2 = userService.update(userDto1);

        User user1 = userRepository.get(uid1);
        assertEquals(user1.getName(), "updated1",
                "Имя пользователя в хранилище должно смениться на updated1");
        assertEquals(user1.getEmail(), "updated1@mail.com",
                "Почтовый адрес в хранилище должен смениться на updated1@mail.com");
    }

    @Test
    @Order(4)
    void addSecondUserTest() {
        UserDto userDto1 = new UserDto(-1L, "user2", "user2@mail.com");
        UserDto userDto2 = userService.add(userDto1);

        User user2 = userRepository.get(userDto2.getId());
        uid2 = user2.getId();
        assertEquals(userRepository.size(), 2, "В хранилище должно содержаться 2 пользователя");
        assertEquals(user2.getEmail(), userDto1.getEmail(),
                "Имя пользователя в хранилище должно быть user2");
        assertEquals(user2.getEmail(), userDto1.getEmail(),
                "Почтовый адрес в хранилище должен быть user2@mail.com");
    }

    @Test
    @Order(5)
    void updateUserNameOnlyTest() {
        UserDto userDto1 = new UserDto(uid1, "name1", null);
        UserDto userDto2 = userService.update(userDto1);

        User user1 = userRepository.get(uid1);
        assertEquals(user1.getName(), "name1",
                "Имя пользователя в хранилище должно было измениться");
        assertEquals(user1.getEmail(), "updated1@mail.com",
                "Почтовый адрес в хранилище измениться не должен");
    }

    @Test
    @Order(6)
    void updateUserEmailOnlyTest() {
        UserDto userDto1 = new UserDto(uid1, null, "name1@mail.com");
        UserDto userDto2 = userService.update(userDto1);

        User user1 = userRepository.get(uid1);
        assertEquals(user1.getName(), "name1",
                "Имя пользователя в хранилище не должно было измениться");
        assertEquals(user1.getEmail(), "name1@mail.com",
                "Почтовый адрес в хранилище должен измениться");
    }

    @Test
    @Order(7)
    void updateUserEmailExistsTest() {
        UserDto userDto1 = new UserDto(uid1, null, "user2@mail.com");

        assertThrows(UserEmailAlreadyExists.class, () -> userService.update(userDto1),
                "email уже используется");
    }

    @Test
    @Order(8)
    void updateUserWithInvalidIdTest() {
        UserDto userDto1 = new UserDto(3, "user3", "user3@mail.com");

        assertThrows(UserNotFoundException.class, () -> userService.update(userDto1),
                "Нет пользователя с таким id");
    }

    @Test
    @Order(9)
    void deleteUserTest() {
        UserDto userDto = userService.remove(uid2);

        assertEquals(userRepository.size(), 1, "Должен остаться только один пользователь");
    }

    @Test
    @Order(10)
    void addUserAfterDeleteTest() {
        UserDto userDto1 = new UserDto(-1L, "user3", "user3@mail.com");
        UserDto userDto2 = userService.add(userDto1);

        assertEquals(userRepository.size(), 2L, "Должено быть 2 пользователя");
        assertEquals(userDto2.getId(), uid2 + 1, "id нового пользователя должен быть: "  + (uid2 + 1));
    }

    @Test
    @Order(11)
    void getAllUsersTest() {
        Collection<UserDto> users = userService.getAll();

        assertEquals(users.size(), 2, "Должено быть 2 пользователя");
    }

    @Test
    @Order(12)
    void getUserWithInvalidIdTest() {
        assertThrows(UserNotFoundException.class, () -> userService.get(-1L),
                "Нет пользователя с таким id");
    }

    @Test
    @Order(13)
    void deleteUserWithInvalidIdTest() {
        assertThrows(UserNotFoundException.class, () -> userService.remove(-1L),
                "Нет пользователя с таким id");
    }
}
