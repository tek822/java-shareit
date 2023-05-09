package ru.practicum.shareit.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserServiceImpl;

import javax.validation.Valid;
import java.util.Collection;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping(path = "/users")
public class UserController {
    @Autowired
    private UserServiceImpl userService;

    @PostMapping
    public UserDto userCreate(@Valid @RequestBody UserDto userDto) {
        return userService.add(userDto);
    }

    @PatchMapping("/{id}")
    public UserDto userUpdate(@PathVariable long id, @RequestBody UserDto userDto) {
        userDto.setId(id);
        return userService.update(userDto);
    }

    @GetMapping("/{id}")
    UserDto userGet(@PathVariable long id) {
        return userService.get(id);
    }

    @GetMapping
    Collection<UserDto> usersGet() {
        return userService.getAll();
    }

    @DeleteMapping("/{id}")
    UserDto userDelete(@PathVariable long id) {
        return userService.remove(id);
    }
}
