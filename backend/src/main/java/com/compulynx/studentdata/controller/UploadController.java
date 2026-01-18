package com.compulynx.studentdata.controller;

import com.compulynx.studentdata.service.StudentUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UploadController {

    private final StudentUploadService studentUploadService;

    public UploadController(StudentUploadService studentUploadService) {
        this.studentUploadService = studentUploadService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadCsv(@RequestParam("file") MultipartFile file) throws IOException {
        int inserted = studentUploadService.uploadCsv(file);
        return ResponseEntity.ok(Map.of("inserted", inserted));
    }
}
