package org.example.debeziumapp2.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.debeziumapp2.entity.enums.ChangeStatus;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Change {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "change_content")
    private String changeContent;

    private String sql;

    @Enumerated(value = EnumType.STRING)
    private ChangeStatus status;

    @Column(name = "table_name")
    private String tableName;
}
