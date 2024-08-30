package org.example.debeziumapp2.service.api;

import org.example.debeziumapp2.dto.ChangeDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ChangeService {

    List<ChangeDto> findAll(String tableName);

    void loadPackage(MultipartFile file);

    void executePackage(Long packageId);

    void executeChange(Long id);
}
