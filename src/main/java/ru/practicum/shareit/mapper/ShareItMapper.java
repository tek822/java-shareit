package ru.practicum.shareit.mapper;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingSimpleDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import javax.annotation.PostConstruct;

@Component
public class ShareItMapper {
    @Autowired
    private ModelMapper modelMapper;

    @PostConstruct
    public void init() {
        modelMapper.createTypeMap(Booking.class, BookingSimpleDto.class).addMappings(mapper -> {
                    mapper.map(src -> src.getBooker().getId(), BookingSimpleDto::setBookerId);
                    mapper.map(src -> src.getItem().getId(), BookingSimpleDto::setItemId);
                }
        );
        modelMapper.createTypeMap(Comment.class, CommentDto.class).addMappings(mapper -> {
                    mapper.map(src -> src.getAuthor().getName(), CommentDto::setAuthorName);
                    mapper.map(src -> src.getItem().getId(), CommentDto::setItemId);
                }
        );

        Converter<Item, Long> request2IdConverter = ctx -> ctx.getSource().getRequest() != null
        ? ctx.getSource().getRequest().getId()
        : null;

        modelMapper.createTypeMap(Item.class, ItemDto.class).addMappings(mapper ->
                mapper.using(request2IdConverter).map(src -> src, ItemDto::setRequestId));
    }

}
