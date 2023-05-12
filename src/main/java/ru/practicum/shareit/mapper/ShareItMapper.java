package ru.practicum.shareit.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ShareItMapper {
    @Autowired
    private ModelMapper modelMapper;

    @PostConstruct
    public void init() {
        //modelMapper.createTypeMap(ItemClass.class, ItemDto.class)
    }
}
