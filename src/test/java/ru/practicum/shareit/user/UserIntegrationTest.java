package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.practicum.shareit.util.Util.getUser;

@Slf4j
@SpringBootTest(properties = "db.name=test", webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class UserIntegrationTest {
    private final UserRepository userRepository;
    private final UserServiceImpl userService;

    private static long uid1;
    private static long uid2;

    @Test
    @Order(1)
    void getInvalidIdUserTest() {
        assertThrows(UserNotFoundException.class, () -> userService.get(-1L),
                "Нет пользователя с таким id");
    }

    @Test
    @Order(2)
    void createFirstUserTest() {
        UserDto userDto1 = new UserDto(-1L, "user1", "user1@mail.com");
        UserDto userDto2 = userService.add(userDto1);

        uid1 = userDto2.getId();
        assertEquals(1, uid1, "Пользователю должен был быть присвоен id: 1");

        User user1 = getUser(userRepository, uid1);
        assertEquals(user1.getName(), userDto1.getName(), "Имя в хранилище должно совпадать с переданным в dto");
        assertEquals(user1.getEmail(), userDto1.getEmail(), "email в хранилище должeн совпадать с переданным в dto");
        assertEquals(1L, userRepository.count(), "Должен быть 1 пользователь");
    }

    @Test
    @Order(3)
    void createUserWithDuplicateEmailTest() {
        UserDto userDto = new UserDto(-1L, "user2", "user1@mail.com");

        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> userService.add(userDto),
                "Почтовый адрес уже используется");
        assertEquals(1L, userRepository.count(), "Должено быть 1 пользователь");
    }

    @Test
    @Order(4)
    void updateUserTest() {
        UserDto userDto1 = new UserDto(uid1, "updated1", "updated1@mail.com");
        UserDto userDto2 = userService.update(userDto1);

        User user1 = getUser(userRepository, uid1);
        assertEquals("updated1", user1.getName(),
                "Имя пользователя в хранилище должно смениться на updated1");
        assertEquals("updated1@mail.com", user1.getEmail(),
                "Почтовый адрес в хранилище должен смениться на updated1@mail.com");
        assertEquals(1L, userRepository.count(), "Должен быть 1 пользователь");
    }

    @Test
    @Order(5)
    void addSecondUserTest() {
        UserDto userDto1 = new UserDto(-1L, "user2", "user2@mail.com");
        UserDto userDto2 = userService.add(userDto1);

        User user2 = getUser(userRepository, userDto2.getId());
        uid2 = user2.getId();
        assertEquals(2, userRepository.count(), "В хранилище должно содержаться 2 пользователя");
        assertEquals(user2.getEmail(), userDto1.getEmail(),
                "Имя пользователя в хранилище должно быть user2");
        assertEquals(user2.getEmail(), userDto1.getEmail(),
                "Почтовый адрес в хранилище должен быть user2@mail.com");
    }

    @Test
    @Order(6)
    void updateUserEmailExistsTest() {
        UserDto userDto1 = new UserDto(uid1, null, "user2@mail.com");

        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> userService.update(userDto1),
                "email уже используется");
        assertEquals(2L, userRepository.count(), "Должено быть 2 пользователя");
    }

    @Test
    @Order(7)
    void updateUserWithInvalidIdTest() {
        UserDto userDto1 = new UserDto(40L, "user3", "user3@mail.com");

        assertThrows(UserNotFoundException.class, () -> userService.update(userDto1),
                "Нет пользователя с таким id");
        assertEquals(2L, userRepository.count(), "Должено быть 2 пользователя");
    }

    @Test
    @Order(8)
    void deleteUserTest() {
        userService.remove(uid2);

        assertEquals(1, userRepository.count(), "Должен остаться только один пользователь");
    }

    @Test
    @Order(9)
    void addUserAfterDeleteTest() {
        UserDto userDto1 = new UserDto(-1L, "user3", "user3@mail.com");
        UserDto userDto2 = userService.add(userDto1);

        assertEquals(2L, userRepository.count(), "Должено быть 2 пользователя");
        assertEquals(uid2 + 1, userDto2.getId(), "id нового пользователя должен быть: "  + (uid2 + 1));
    }

    @Test
    @Order(10)
    void getAllUsersTest() {
        Collection<UserDto> users = userService.getAll();

        assertEquals(2, users.size(), "Должено быть 2 пользователя");
    }

    @Test
    @Order(11)
    void getUserWithInvalidIdTest() {
        assertThrows(UserNotFoundException.class, () -> userService.get(-1L),
                "Нет пользователя с таким id");
    }

    @Test
    @Order(12)
    void deleteUserWithInvalidIdTest() {
        assertThrows(org.springframework.dao.EmptyResultDataAccessException.class, () -> userService.remove(-1L),
                "Нет пользователя с таким id");
    }
}
