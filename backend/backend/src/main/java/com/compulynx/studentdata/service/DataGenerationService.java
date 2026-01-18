package com.compulynx.studentdata.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
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
    private static final int ROW_WINDOW = 5000;
    private static final int FLUSH_INTERVAL = 5000;
    private static final int OUTPUT_BUFFER_SIZE = 256 * 1024; // 256KB buffer

    private final FilePathService filePathService;

    // Pre-computed date range for faster random date generation
    private static final LocalDate START_DATE = LocalDate.of(2000, 1, 1);
    private static final long DATE_RANGE_DAYS = ChronoUnit.DAYS.between(START_DATE, LocalDate.of(2010, 12, 31)) + 1;

    public DataGenerationService(FilePathService filePathService) {
        this.filePathService = filePathService;
    }

    public Path generateExcel(int count) throws IOException {
        Path output = filePathService.buildFilePath("students", ".xlsx");
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(ROW_WINDOW);
             OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(output), OUTPUT_BUFFER_SIZE)) {

            workbook.setCompressTempFiles(false);
            SXSSFSheet sheet = workbook.createSheet("students");
            sheet.setRandomAccessWindowSize(ROW_WINDOW);

            createHeader(sheet);

            // Reuse StringBuilder for string generation
            StringBuilder sb = new StringBuilder(8);

            for (int i = 0; i < count; i++) {
                Row row = sheet.createRow(i + 1);
                fillRow(row, i + 1, sb);

                if ((i + 1) % FLUSH_INTERVAL == 0) {
                    sheet.flushRows(FLUSH_INTERVAL);
                }
            }

            workbook.write(outputStream);
            workbook.dispose(); // Clean up temp files
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

    private void fillRow(Row row, int studentId, StringBuilder sb) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        row.createCell(0).setCellValue(studentId);
        row.createCell(1).setCellValue(randomString(sb, random));
        row.createCell(2).setCellValue(randomString(sb, random));
        row.createCell(3).setCellValue(randomDate(random).toString());
        row.createCell(4).setCellValue(CLASSES[random.nextInt(CLASSES.length)]);
        row.createCell(5).setCellValue(random.nextInt(55, 76));
    }

    private String randomString(StringBuilder sb, ThreadLocalRandom random) {
        sb.setLength(0);
        int length = random.nextInt(3, 9);
        for (int i = 0; i < length; i++) {
            sb.append((char) ('A' + random.nextInt(26)));
        }
        return sb.toString();
    }

    private LocalDate randomDate(ThreadLocalRandom random) {
        return START_DATE.plusDays(random.nextLong(DATE_RANGE_DAYS));
    }
}
