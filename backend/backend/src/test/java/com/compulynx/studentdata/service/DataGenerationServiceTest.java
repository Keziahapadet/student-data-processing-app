package com.compulynx.studentdata.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DataGenerationServiceTest {

    private DataGenerationService dataGenerationService;

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
        dataGenerationService = new DataGenerationService(filePathService);
    }

    @Test
    void generateExcel_shouldCreateFileWithCorrectRowCount() throws IOException {
        int count = 100;

        Path result = dataGenerationService.generateExcel(count);

        assertTrue(result.toFile().exists(), "Excel file should be created");
        assertTrue(result.toString().endsWith(".xlsx"), "File should have .xlsx extension");

        try (FileInputStream fis = new FileInputStream(result.toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            // +1 for header row
            assertEquals(count + 1, sheet.getPhysicalNumberOfRows(), "Should have correct number of rows");
        }
    }

    @Test
    void generateExcel_shouldHaveCorrectHeaders() throws IOException {
        Path result = dataGenerationService.generateExcel(10);

        try (FileInputStream fis = new FileInputStream(result.toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row header = sheet.getRow(0);

            assertEquals("studentId", header.getCell(0).getStringCellValue());
            assertEquals("firstName", header.getCell(1).getStringCellValue());
            assertEquals("lastName", header.getCell(2).getStringCellValue());
            assertEquals("dob", header.getCell(3).getStringCellValue());
            assertEquals("class", header.getCell(4).getStringCellValue());
            assertEquals("score", header.getCell(5).getStringCellValue());
        }
    }

    @Test
    void generateExcel_shouldHaveValidDataInRows() throws IOException {
        Path result = dataGenerationService.generateExcel(10);

        try (FileInputStream fis = new FileInputStream(result.toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row dataRow = sheet.getRow(1);

            // Student ID should be 1
            assertEquals(1, (int) dataRow.getCell(0).getNumericCellValue());

            // First and last name should be non-empty strings
            String firstName = dataRow.getCell(1).getStringCellValue();
            String lastName = dataRow.getCell(2).getStringCellValue();
            assertNotNull(firstName);
            assertNotNull(lastName);
            assertTrue(firstName.length() >= 3 && firstName.length() <= 8);
            assertTrue(lastName.length() >= 3 && lastName.length() <= 8);

            // DOB should be a valid date string
            String dobStr = dataRow.getCell(3).getStringCellValue();
            LocalDate dob = LocalDate.parse(dobStr);
            assertTrue(dob.getYear() >= 2000 && dob.getYear() <= 2010);

            // Class should be one of Class1-Class5
            String studentClass = dataRow.getCell(4).getStringCellValue();
            assertTrue(studentClass.matches("Class[1-5]"));

            // Score should be between 55 and 75
            int score = (int) dataRow.getCell(5).getNumericCellValue();
            assertTrue(score >= 55 && score <= 75);
        }
    }

    @Test
    void generateExcel_withZeroCount_shouldCreateEmptyFileWithHeader() throws IOException {
        Path result = dataGenerationService.generateExcel(0);

        try (FileInputStream fis = new FileInputStream(result.toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            assertEquals(1, sheet.getPhysicalNumberOfRows(), "Should only have header row");
        }
    }

    @Test
    void generateExcel_withLargeCount_shouldComplete() throws IOException {
        int count = 10000;

        long startTime = System.currentTimeMillis();
        Path result = dataGenerationService.generateExcel(count);
        long duration = System.currentTimeMillis() - startTime;

        assertTrue(result.toFile().exists());
        assertTrue(result.toFile().length() > 0);
        // Performance assertion - should complete in reasonable time
        assertTrue(duration < 30000, "Generation should complete within 30 seconds");
    }
}
