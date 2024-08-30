package org.example.debeziumapp2.repository.api;

import org.example.debeziumapp2.entity.Change;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChangeRepository extends JpaRepository<Change, Long> {

    List<Change> findAllByTableName(String tableName);
}
