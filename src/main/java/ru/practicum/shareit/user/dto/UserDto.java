package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private long id;
    @NotBlank(message = "Имя пользователя не может быть null или blank")
    private String name;
    @NotBlank(message = "Email не может быть null или blank")
    @Email(message = "Некорректный адрес почты")
    private String email;
}
