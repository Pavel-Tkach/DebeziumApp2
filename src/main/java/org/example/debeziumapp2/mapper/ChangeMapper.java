package org.example.debeziumapp2.mapper;

import org.example.debeziumapp2.dto.ChangeDto;
import org.example.debeziumapp2.entity.Change;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChangeMapper {

    ChangeDto toDto(Change change);
}
