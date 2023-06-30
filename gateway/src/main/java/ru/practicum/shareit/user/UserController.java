package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.util.Create;
import ru.practicum.shareit.util.Update;

@Slf4j
@RestController
@RequestMapping(path = "/users")
public class UserController {
    @Autowired
    private UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> userCreate(@Validated(Create.class) @RequestBody UserDto userDto) {
        log.info("POST запрос для user: {}", userDto);
        return userClient.addUser(userDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> userUpdate(@PathVariable long id, @Validated(Update.class) @RequestBody UserDto userDto) {
        log.info("PATCH запрос для id: {}, user: {}", id, userDto);
        userDto.setId(id);
        return userClient.updateUser(id, userDto);
    }

    @GetMapping("/{id}")
    ResponseEntity<Object> userGet(@PathVariable long id) {
        log.info("GET запрос для id: {}", id);
        return userClient.getUser(id);
    }

    @GetMapping
    ResponseEntity<Object> usersGet() {
        log.info("GET запрос для all users");
        return userClient.getAllUsers();
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Object> userDelete(@PathVariable long id) {
        log.info("DELETE запрос для user c id: {}", id);
        return userClient.removeUser(id);
    }
}