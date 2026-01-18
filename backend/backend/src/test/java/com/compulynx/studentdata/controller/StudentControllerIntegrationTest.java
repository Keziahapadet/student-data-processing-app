package com.compulynx.studentdata.controller;

import com.compulynx.studentdata.model.Student;
import com.compulynx.studentdata.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StudentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
    }

    @Test
    void listStudents_shouldReturnEmptyPage_whenNoStudentsExist() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)));
    }

    @Test
    void listStudents_shouldReturnStudents_whenStudentsExist() throws Exception {
        List<Student> students = createTestStudents(5);
        studentRepository.saveAll(students);

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements", is(5)));
    }

    @Test
    void listStudents_shouldSupportPagination() throws Exception {
        List<Student> students = createTestStudents(25);
        studentRepository.saveAll(students);

        mockMvc.perform(get("/api/students")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.totalElements", is(25)))
                .andExpect(jsonPath("$.totalPages", is(3)))
                .andExpect(jsonPath("$.number", is(0)));
    }

    @Test
    void listStudents_shouldFilterByStudentId() throws Exception {
        List<Student> students = createTestStudents(10);
        studentRepository.saveAll(students);

        mockMvc.perform(get("/api/students")
                        .param("studentId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].studentId", is(5)));
    }

    @Test
    void listStudents_shouldFilterByClass() throws Exception {
        List<Student> students = createTestStudents(10);
        studentRepository.saveAll(students);

        mockMvc.perform(get("/api/students")
                        .param("class", "Class1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].studentClass", everyItem(is("Class1"))));
    }

    @Test
    void listStudents_shouldReturnEmptyForNonExistentStudentId() throws Exception {
        List<Student> students = createTestStudents(5);
        studentRepository.saveAll(students);

        mockMvc.perform(get("/api/students")
                        .param("studentId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void exportExcel_shouldReturnExcelFile() throws Exception {
        List<Student> students = createTestStudents(5);
        studentRepository.saveAll(students);

        mockMvc.perform(get("/api/students/export/excel"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("students.xlsx")))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
    }

    @Test
    void exportCsv_shouldReturnCsvFile() throws Exception {
        List<Student> students = createTestStudents(5);
        studentRepository.saveAll(students);

        mockMvc.perform(get("/api/students/export/csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("students.csv")))
                .andExpect(content().contentType(MediaType.TEXT_PLAIN));
    }

    @Test
    void exportPdf_shouldReturnPdfFile() throws Exception {
        List<Student> students = createTestStudents(5);
        studentRepository.saveAll(students);

        mockMvc.perform(get("/api/students/export/pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("students.pdf")))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    private List<Student> createTestStudents(int count) {
        List<Student> students = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            students.add(new Student(
                    (long) i,
                    "First" + i,
                    "Last" + i,
                    LocalDate.of(2005, 6, 15),
                    "Class" + ((i % 5) + 1),
                    70 + (i % 20)
            ));
        }
        return students;
    }
}
