package com.compulynx.studentdata.service;

import com.github.pjfanning.xlsx.StreamingReader;
import com.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

@Service
public class DataProcessingService {

    private static final int MAX_EXCEL_BYTES = 500_000_000;

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
                     .rowCacheSize(100)
                     .bufferSize(2048)
                     .open(inputStream);
             Writer writer = Files.newBufferedWriter(output);
             CSVWriter csvWriter = new CSVWriter(writer)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) {
                rowIterator.next(); // skip header
            }
            String[] header = {"studentId", "firstName", "lastName", "dob", "class", "score"};
            csvWriter.writeNext(header, false);
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                String[] line = new String[6];
                line[0] = stringValue(row.getCell(0));
                line[1] = stringValue(row.getCell(1));
                line[2] = stringValue(row.getCell(2));
                line[3] = stringValue(row.getCell(3));
                line[4] = stringValue(row.getCell(4));
                int baseScore = parseInt(row.getCell(5));
                line[5] = String.valueOf(baseScore + 10);
                csvWriter.writeNext(line, false);
            }
        }
        return output;
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
