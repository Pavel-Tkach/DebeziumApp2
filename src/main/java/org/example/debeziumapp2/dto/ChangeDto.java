package org.example.debeziumapp2.dto;

import lombok.Data;

@Data
public class ChangeDto {

    private Long id;

    private String sql;

    private String changeContent;

    private String status;

    private String tableName;
}
