package com.compulynx.studentdata.controller;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DataControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void generate_shouldCreateExcelFile_withDefaultCount() throws Exception {
        mockMvc.perform(post("/api/generate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filePath", notNullValue()))
                .andExpect(jsonPath("$.filePath", endsWith(".xlsx")));
    }

    @Test
    void generate_shouldCreateExcelFile_withCustomCount() throws Exception {
        mockMvc.perform(post("/api/generate")
                        .param("count", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filePath", notNullValue()));
    }

    @Test
    void generate_shouldCreateExcelFile_withSmallCount() throws Exception {
        mockMvc.perform(post("/api/generate")
                        .param("count", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filePath", notNullValue()));
    }

    @Test
    void process_shouldConvertExcelToCsv() throws Exception {
        byte[] excelData = createTestExcel(10);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                excelData
        );

        mockMvc.perform(multipart("/api/process")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filePath", notNullValue()))
                .andExpect(jsonPath("$.filePath", endsWith(".csv")));
    }

    @Test
    void process_shouldHandleLargerFile() throws Exception {
        byte[] excelData = createTestExcel(1000);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large-test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                excelData
        );

        mockMvc.perform(multipart("/api/process")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filePath", notNullValue()));
    }

    @Test
    void process_shouldRejectEmptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/process")
                        .file(emptyFile))
                .andExpect(status().is5xxServerError());
    }

    private byte[] createTestExcel(int rowCount) throws Exception {
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
                row.createCell(5).setCellValue(60 + (i % 15));
            }

            workbook.write(out);
            workbook.dispose();
            return out.toByteArray();
        }
    }
}
