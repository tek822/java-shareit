package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserRepository {

    User add(User user);

    User update(User user);

    User get(long id);

    Collection<User> getUsers();

    User delete(long id);

    long size();
}
