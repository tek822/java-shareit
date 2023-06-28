package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.util.Create;
import ru.practicum.shareit.util.Update;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private long id;
    @NotBlank(groups = {Create.class}, message = "Имя пользователя не может быть null или blank")
    private String name;
    @NotBlank(groups = {Create.class}, message = "Email не может быть null или blank")
    @Email(groups = {Update.class, Create.class}, message = "Некорректный адрес почты")
    private String email;
}
