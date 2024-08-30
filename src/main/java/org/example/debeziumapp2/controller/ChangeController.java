package org.example.debeziumapp2.controller;

import lombok.RequiredArgsConstructor;
import org.example.debeziumapp2.dto.ChangeDto;
import org.example.debeziumapp2.service.api.ChangeService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/changes")
public class ChangeController {

    private final ChangeService changeService;

    @GetMapping
    public List<ChangeDto> findAll(@RequestParam String tableName) {
        return changeService.findAll(tableName);
    }

    @GetMapping("/package/load")
    public void loadPackage(@RequestParam MultipartFile zip) {
        changeService.loadPackage(zip);
    }

    @PostMapping("/package/{packageId}/execute")
    public void executePackage(@PathVariable() Long packageId) {
        changeService.executePackage(packageId);
    }

    @PostMapping("/{changeId}/execute")
    public void executeChange(@PathVariable Long changeId) {
        changeService.executeChange(changeId);
    }
}
