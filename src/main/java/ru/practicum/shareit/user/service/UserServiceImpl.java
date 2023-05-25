package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.stream.Collectors;

import static ru.practicum.shareit.util.Util.getUser;

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
        log.info("Добавлен пользователь: {}", userDto);
        return modelMapper.map(userRepository.save(user), UserDto.class);
    }

    @Override
    public UserDto update(UserDto userDto) {
        User user = getUser(userRepository, userDto.getId());
        User update = modelMapper.map(userDto, User.class);
        modelMapper.map(update, user);
        log.info("Обновлены данные пользователя: {}", userDto);
        return modelMapper.map(userRepository.save(user), UserDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto get(long id) {
        User user = getUser(userRepository, id);
        log.info("Запрошены данные пользователя с id: {}", id);
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<UserDto> getAll() {
        log.info("Запрошены данные всех пользователей");
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void remove(long id) {
        log.info("Удалены данные пользователя с id: {}", id);
        userRepository.deleteById(id);
    }

    private UserDto convertToDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }
}
