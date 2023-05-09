package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDto add(UserDto userDto) {
        User user = modelMapper.map(userDto, User.class);
        return modelMapper.map(userRepository.add(user), UserDto.class);
    }

    @Override
    public UserDto update(UserDto userDto) {
        User user = new User(userRepository.get(userDto.getId()));
        User update = modelMapper.map(userDto, User.class);
        modelMapper.map(update, user);
        return modelMapper.map(userRepository.update(user), UserDto.class);
    }

    @Override
    public UserDto get(long id) {
        return modelMapper.map(userRepository.get(id), UserDto.class);
    }

    @Override
    public Collection<UserDto> getAll() {
        return userRepository.getUsers().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto remove(long id) {
        return modelMapper.map(userRepository.delete(id), UserDto.class);
    }

    private UserDto convertToDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }
}
