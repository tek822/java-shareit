package ru.practicum.shareit.user.repository;

import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.user.model.User;

import javax.validation.Valid;
import java.util.Collection;

@Validated
public interface UserRepository {

    User add(User user);

    User update(@Valid User user);

    User get(long id);

    Collection<User> getUsers();

    User delete(long id);

    long size();
}
