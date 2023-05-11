package ru.practicum.shareit.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * TODO Sprint add-controllers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private long id;
    @NotBlank(message = "Имя пользователя не может быть null или blank")
    private String name;
    @NotBlank(message = "Email не может быть null или blank")
    @Email(message = "Некорректный адрес почты")
    private String email;

    public User(User origin) {
        this.id = origin.id;
        this.name = origin.name;
        this.email = origin.email;
    }
}
