package com.compulynx.studentdata.controller;

import com.compulynx.studentdata.service.DataGenerationService;
import com.compulynx.studentdata.service.DataProcessingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DataController {

    private final DataGenerationService dataGenerationService;
    private final DataProcessingService dataProcessingService;

    public DataController(DataGenerationService dataGenerationService, DataProcessingService dataProcessingService) {
        this.dataGenerationService = dataGenerationService;
        this.dataProcessingService = dataProcessingService;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generate(@RequestParam(defaultValue = "1000") int count) throws IOException {
        Path file = dataGenerationService.generateExcel(count);
        return ResponseEntity.ok(Map.of("filePath", file.toString()));
    }

    @PostMapping("/process")
    public ResponseEntity<Map<String, String>> process(@RequestParam("file") MultipartFile file) throws IOException {
        Path csv = dataProcessingService.convertExcelToCsv(file);
        return ResponseEntity.ok(Map.of("filePath", csv.toString()));
    }
}
