package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "items")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    private Boolean available;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    //@OneToOne
    //@JoinColumn(name = "request_id")
    @Transient
    private ItemRequest request;

    public Item(Item orig) {
        this.id = orig.id;
        this.name = orig.name;
        this.description = orig.description;
        this.available = orig.available;
        this.owner = new User(orig.owner);
        this.request = orig.request;
    }
}
