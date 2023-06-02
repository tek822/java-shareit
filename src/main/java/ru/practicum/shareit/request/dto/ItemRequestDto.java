package ru.practicum.shareit.request.dto;

import lombok.Data;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemRequestDto {
    Long id;
    @NotBlank
    String description;
    LocalDateTime created;
    List<ItemDto> items;
}
