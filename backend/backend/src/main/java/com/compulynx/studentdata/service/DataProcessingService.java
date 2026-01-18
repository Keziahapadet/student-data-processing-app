package com.compulynx.studentdata.service;

import com.github.pjfanning.xlsx.StreamingReader;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

@Service
public class DataProcessingService {

    private static final int MAX_EXCEL_BYTES = 500_000_000;
    private static final int ROW_CACHE_SIZE = 1000;      // Increased from 100
    private static final int BUFFER_SIZE = 64 * 1024;    // 64KB, increased from 2KB
    private static final int WRITE_BUFFER_SIZE = 256 * 1024; // 256KB write buffer
    private static final int BATCH_SIZE = 1000;          // Write in batches

    private final FilePathService filePathService;

    public DataProcessingService(FilePathService filePathService) {
        this.filePathService = filePathService;
        applyPoiSafetyOverrides();
    }

    private static void applyPoiSafetyOverrides() {
        IOUtils.setByteArrayMaxOverride(MAX_EXCEL_BYTES);
        ZipSecureFile.setMinInflateRatio(0.0d);
    }

    public Path convertExcelToCsv(MultipartFile file) throws IOException {
        Path output = filePathService.buildFilePath("students-processed", ".csv");

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = StreamingReader.builder()
                     .rowCacheSize(ROW_CACHE_SIZE)
                     .bufferSize(BUFFER_SIZE)
                     .open(inputStream);
             BufferedWriter writer = Files.newBufferedWriter(output)) {

            // Use larger buffer for writer
            StringBuilder batch = new StringBuilder(WRITE_BUFFER_SIZE);

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip header
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            // Write CSV header
            writer.write("studentId,firstName,lastName,dob,class,score\n");

            int rowCount = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                appendRowToBatch(batch, row);
                rowCount++;

                // Flush batch periodically
                if (rowCount % BATCH_SIZE == 0) {
                    writer.write(batch.toString());
                    batch.setLength(0);
                }
            }

            // Write remaining rows
            if (batch.length() > 0) {
                writer.write(batch.toString());
            }
        }

        return output;
    }

    private void appendRowToBatch(StringBuilder batch, Row row) {
        batch.append(stringValue(row.getCell(0))).append(',');
        batch.append(stringValue(row.getCell(1))).append(',');
        batch.append(stringValue(row.getCell(2))).append(',');
        batch.append(stringValue(row.getCell(3))).append(',');
        batch.append(stringValue(row.getCell(4))).append(',');

        int baseScore = parseInt(row.getCell(5));
        batch.append(baseScore + 10).append('\n');
    }

    private String stringValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case STRING -> cell.getStringCellValue();
            default -> cell.toString();
        };
    }

    private int parseInt(Cell cell) {
        if (cell == null) {
            return 0;
        }
        return switch (cell.getCellType()) {
            case STRING -> Integer.parseInt(cell.getStringCellValue().trim());
            case NUMERIC -> (int) Math.round(cell.getNumericCellValue());
            default -> 0;
        };
    }
}
