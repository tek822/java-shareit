package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.exception.UserEmailAlreadyExists;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import javax.validation.Valid;
import java.util.Collection;
import java.util.HashMap;

@Slf4j
@Repository("InMemoryUserRepository")
@Validated
public class UserInMemoryRepositoryImpl implements UserRepository {
    private long nextId = 1;
    private final HashMap<Long, User> users = new HashMap<>();
    private final HashMap<String, Long> emails = new HashMap<>();

    @Override
    public User add(@Valid User user) {
        String email = user.getEmail();
        if (emails.containsKey(email)) {
            throw new UserEmailAlreadyExists("Email " + email + " уже используется");
        }

        user.setId(nextId);
        log.info("Добавлен пользователь с id {}", nextId);
        users.put(nextId, user);
        emails.put(email, nextId++);
        return user;
    }

    @Override
    public User update(@Valid User user) {
        long id = user.getId();
        if (!users.containsKey(id)) {
            log.info("Не найден пользователь с id {}", id);
            throw new UserNotFoundException("Не найден пользователь с id: " + id);
        }

        String newEmail = user.getEmail();
        String oldEmail = users.get(id).getEmail();
        if (!newEmail.equals(oldEmail)) {
            if (emails.containsKey(newEmail)) {
                throw new UserEmailAlreadyExists("Email " + newEmail + " уже используется");
            } else {
                emails.remove(oldEmail);
                emails.put(newEmail, id);
            }
        }

        log.info("Обновлен пользователь с id: {}", id);
        users.replace(id, user);
        return user;
    }

    @Override
    public User get(long id) {
        if (!users.containsKey(id)) {
            log.info("Не найден пользователь с id {}", id);
            throw new UserNotFoundException("Не найден пользователь с id: " + id);
        }
        return users.get(id);
    }

    @Override
    public Collection<User> getUsers() {
        log.info("Запрошено {} записей пользователей", users.size());
        return users.values();
    }

    @Override
    public User delete(long id) {
        if (!users.containsKey(id)) {
            log.info("Не найден пользователь с id {}", id);
            throw new UserNotFoundException("Не найден пользователь с id: " + id);
        }
        User user = users.get(id);
        log.info("Удален пользователь с id: {}", id);
        users.remove(id);
        emails.remove(user.getEmail());
        return user;
    }

    @Override
    public long size() {
        return users.size();
    }
}
