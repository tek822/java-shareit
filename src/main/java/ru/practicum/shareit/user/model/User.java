package ru.practicum.shareit.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
