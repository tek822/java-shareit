package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {

    UserDto add(UserDto userDto);

    UserDto update(UserDto userDto);

    UserDto get(long id);

    Collection<UserDto> getAll();

    UserDto remove(long id);
}
