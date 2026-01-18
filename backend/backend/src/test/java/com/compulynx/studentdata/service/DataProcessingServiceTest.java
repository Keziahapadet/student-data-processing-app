package com.compulynx.studentdata.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataProcessingServiceTest {

    private DataProcessingService dataProcessingService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        FilePathService filePathService = new FilePathService() {
            @Override
            public Path buildFilePath(String prefix, String suffix) {
                return tempDir.resolve(prefix + System.currentTimeMillis() + suffix);
            }
        };
        dataProcessingService = new DataProcessingService(filePathService);
    }

    @Test
    void convertExcelToCsv_shouldCreateCsvFile() throws IOException {
        byte[] excelData = createTestExcel(10);
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelData);

        Path result = dataProcessingService.convertExcelToCsv(file);

        assertTrue(result.toFile().exists(), "CSV file should be created");
        assertTrue(result.toString().endsWith(".csv"), "File should have .csv extension");
    }

    @Test
    void convertExcelToCsv_shouldHaveCorrectHeaders() throws IOException {
        byte[] excelData = createTestExcel(5);
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelData);

        Path result = dataProcessingService.convertExcelToCsv(file);
        List<String> lines = Files.readAllLines(result);

        assertEquals("studentId,firstName,lastName,dob,class,score", lines.get(0));
    }

    @Test
    void convertExcelToCsv_shouldAddTenToScore() throws IOException {
        byte[] excelData = createTestExcelWithKnownScore(5, 60); // Score = 60
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelData);

        Path result = dataProcessingService.convertExcelToCsv(file);
        List<String> lines = Files.readAllLines(result);

        // Skip header, check first data row
        String[] tokens = lines.get(1).split(",");
        assertEquals("70", tokens[5], "Score should be increased by 10");
    }

    @Test
    void convertExcelToCsv_shouldHaveCorrectRowCount() throws IOException {
        int rowCount = 100;
        byte[] excelData = createTestExcel(rowCount);
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelData);

        Path result = dataProcessingService.convertExcelToCsv(file);
        List<String> lines = Files.readAllLines(result);

        // +1 for header
        assertEquals(rowCount + 1, lines.size(), "CSV should have same row count as Excel");
    }

    @Test
    void convertExcelToCsv_shouldPreserveAllFields() throws IOException {
        byte[] excelData = createTestExcelWithKnownData();
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelData);

        Path result = dataProcessingService.convertExcelToCsv(file);
        List<String> lines = Files.readAllLines(result);

        String[] tokens = lines.get(1).split(",");
        assertEquals("1", tokens[0], "Student ID should be preserved");
        assertEquals("John", tokens[1], "First name should be preserved");
        assertEquals("Doe", tokens[2], "Last name should be preserved");
        assertEquals("2005-06-15", tokens[3], "DOB should be preserved");
        assertEquals("Class1", tokens[4], "Class should be preserved");
        assertEquals("75", tokens[5], "Score should be 65 + 10 = 75");
    }

    private byte[] createTestExcel(int rowCount) throws IOException {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("students");

            // Header
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("studentId");
            header.createCell(1).setCellValue("firstName");
            header.createCell(2).setCellValue("lastName");
            header.createCell(3).setCellValue("dob");
            header.createCell(4).setCellValue("class");
            header.createCell(5).setCellValue("score");

            // Data rows
            for (int i = 0; i < rowCount; i++) {
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue("First" + i);
                row.createCell(2).setCellValue("Last" + i);
                row.createCell(3).setCellValue("2005-06-15");
                row.createCell(4).setCellValue("Class" + ((i % 5) + 1));
                row.createCell(5).setCellValue(60);
            }

            workbook.write(out);
            workbook.dispose();
            return out.toByteArray();
        }
    }

    private byte[] createTestExcelWithKnownScore(int rowCount, int score) throws IOException {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("students");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("studentId");
            header.createCell(1).setCellValue("firstName");
            header.createCell(2).setCellValue("lastName");
            header.createCell(3).setCellValue("dob");
            header.createCell(4).setCellValue("class");
            header.createCell(5).setCellValue("score");

            for (int i = 0; i < rowCount; i++) {
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue("First" + i);
                row.createCell(2).setCellValue("Last" + i);
                row.createCell(3).setCellValue("2005-06-15");
                row.createCell(4).setCellValue("Class1");
                row.createCell(5).setCellValue(score);
            }

            workbook.write(out);
            workbook.dispose();
            return out.toByteArray();
        }
    }

    private byte[] createTestExcelWithKnownData() throws IOException {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("students");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("studentId");
            header.createCell(1).setCellValue("firstName");
            header.createCell(2).setCellValue("lastName");
            header.createCell(3).setCellValue("dob");
            header.createCell(4).setCellValue("class");
            header.createCell(5).setCellValue("score");

            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue(1);
            row.createCell(1).setCellValue("John");
            row.createCell(2).setCellValue("Doe");
            row.createCell(3).setCellValue("2005-06-15");
            row.createCell(4).setCellValue("Class1");
            row.createCell(5).setCellValue(65);

            workbook.write(out);
            workbook.dispose();
            return out.toByteArray();
        }
    }
}
