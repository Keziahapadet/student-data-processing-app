package com.compulynx.studentdata.controller;

import com.compulynx.studentdata.model.Student;
import com.compulynx.studentdata.service.ExportService;
import com.compulynx.studentdata.service.StudentService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;
    private final ExportService exportService;

    public StudentController(StudentService studentService, ExportService exportService) {
        this.studentService = studentService;
        this.exportService = exportService;
    }

    @GetMapping
    public Page<Student> listStudents(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size,
                                      @RequestParam(required = false) Long studentId,
                                      @RequestParam(value = "class", required = false) String studentClass) {
        String normalizedClass = (studentClass != null && studentClass.isBlank()) ? null : studentClass;
        return studentService.getStudents(studentId, normalizedClass, page, size);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<ByteArrayResource> exportExcel() throws IOException {
        byte[] data = exportService.exportExcel();
        return buildResponse(data, "students.xlsx", MediaType.APPLICATION_OCTET_STREAM);
    }

    @GetMapping("/export/csv")
    public ResponseEntity<ByteArrayResource> exportCsv() throws IOException {
        byte[] data = exportService.exportCsv();
        return buildResponse(data, "students.csv", MediaType.TEXT_PLAIN);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<ByteArrayResource> exportPdf() throws IOException {
        byte[] data = exportService.exportPdf();
        return buildResponse(data, "students.pdf", MediaType.APPLICATION_PDF);
    }

    private ResponseEntity<ByteArrayResource> buildResponse(byte[] data, String filename, MediaType mediaType) {
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentLength(data.length)
                .contentType(mediaType)
                .body(resource);
    }
}
