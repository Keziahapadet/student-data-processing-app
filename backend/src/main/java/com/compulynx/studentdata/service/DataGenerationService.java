package com.compulynx.studentdata.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class DataGenerationService {

    private static final String[] CLASSES = {"Class1", "Class2", "Class3", "Class4", "Class5"};
    private final FilePathService filePathService;

    public DataGenerationService(FilePathService filePathService) {
        this.filePathService = filePathService;
    }

    public Path generateExcel(int count) throws IOException {
        Path output = filePathService.buildFilePath("students", ".xlsx");
        try (Workbook workbook = new SXSSFWorkbook(500);
             OutputStream outputStream = Files.newOutputStream(output)) {
            Sheet sheet = workbook.createSheet("students");
            createHeader(sheet);
            for (int i = 0; i < count; i++) {
                Row row = sheet.createRow(i + 1);
                fillRow(row, i + 1);
                if (i % 2000 == 0 && sheet instanceof SXSSFSheet sxssfSheet) {
                    sxssfSheet.flushRows(2000);
                }
            }
            workbook.write(outputStream);
        }
        return output;
    }

    private void createHeader(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("studentId");
        header.createCell(1).setCellValue("firstName");
        header.createCell(2).setCellValue("lastName");
        header.createCell(3).setCellValue("dob");
        header.createCell(4).setCellValue("class");
        header.createCell(5).setCellValue("score");
    }

    private void fillRow(Row row, int studentId) {
        Cell c0 = row.createCell(0);
        c0.setCellValue(studentId);
        row.createCell(1).setCellValue(randomString());
        row.createCell(2).setCellValue(randomString());
        row.createCell(3).setCellValue(randomDate().toString());
        row.createCell(4).setCellValue(randomClass());
        row.createCell(5).setCellValue(randomScore());
    }

    private String randomString() {
        int length = ThreadLocalRandom.current().nextInt(3, 9);
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char ch = (char) ('A' + ThreadLocalRandom.current().nextInt(26));
            sb.append(ch);
        }
        return sb.toString();
    }

    private LocalDate randomDate() {
        LocalDate start = LocalDate.of(2000, 1, 1);
        long days = ChronoUnit.DAYS.between(start, LocalDate.of(2010, 12, 31));
        return start.plusDays(ThreadLocalRandom.current().nextLong(days + 1));
    }

    private String randomClass() {
        return CLASSES[ThreadLocalRandom.current().nextInt(CLASSES.length)];
    }

    private int randomScore() {
        return ThreadLocalRandom.current().nextInt(55, 76);
    }
}
