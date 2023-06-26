package ru.practicum.shareit.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserServiceImpl;
import ru.practicum.shareit.util.Create;
import ru.practicum.shareit.util.Update;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping(path = "/users")
public class UserController {
    @Autowired
    private UserServiceImpl userService;

    @PostMapping
    public UserDto userCreate(@Validated(Create.class) @RequestBody UserDto userDto) {
        log.info("POST запрос для user: {}", userDto);
        return userService.add(userDto);
    }

    @PatchMapping("/{id}")
    public UserDto userUpdate(@PathVariable long id, @Validated(Update.class) @RequestBody UserDto userDto) {
        log.info("PATCH запрос для id: {}, user: {}", id, userDto);
        userDto.setId(id);
        return userService.update(userDto);
    }

    @GetMapping("/{id}")
    UserDto userGet(@PathVariable long id) {
        log.info("GET запрос для id: {}", id);
        return userService.get(id);
    }

    @GetMapping
    Collection<UserDto> usersGet() {
        log.info("GET запрос для all users");
        return userService.getAll();
    }

    @DeleteMapping("/{id}")
    void userDelete(@PathVariable long id) {
        log.info("DELETE запрос для user c id: {}", id);
        userService.remove(id);
    }
}
