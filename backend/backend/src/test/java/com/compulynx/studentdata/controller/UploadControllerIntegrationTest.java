package com.compulynx.studentdata.controller;

import com.compulynx.studentdata.model.Student;
import com.compulynx.studentdata.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UploadControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
    }

    @Test
    void uploadCsv_shouldInsertStudentsToDatabase() throws Exception {
        String csvContent = """
                studentId,firstName,lastName,dob,class,score
                1,John,Doe,2005-06-15,Class1,70
                2,Jane,Smith,2006-03-20,Class2,75
                3,Bob,Wilson,2005-11-10,Class1,80
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "students.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/upload")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inserted", is(3)));

        assertEquals(3, studentRepository.count());
    }

    @Test
    void uploadCsv_shouldAddFiveToScore() throws Exception {
        String csvContent = """
                studentId,firstName,lastName,dob,class,score
                1,John,Doe,2005-06-15,Class1,70
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "students.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/upload")
                        .file(file))
                .andExpect(status().isOk());

        Optional<Student> student = studentRepository.findById(1L);
        assertTrue(student.isPresent());
        assertEquals(75, student.get().getScore(), "Score should be 70 + 5 = 75");
    }

    @Test
    void uploadCsv_shouldStoreCorrectData() throws Exception {
        String csvContent = """
                studentId,firstName,lastName,dob,class,score
                1,John,Doe,2005-06-15,Class1,70
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "students.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/upload")
                        .file(file))
                .andExpect(status().isOk());

        Optional<Student> student = studentRepository.findById(1L);
        assertTrue(student.isPresent());
        assertEquals("John", student.get().getFirstName());
        assertEquals("Doe", student.get().getLastName());
        assertEquals("Class1", student.get().getStudentClass());
    }

    @Test
    void uploadCsv_shouldHandleLargeFile() throws Exception {
        StringBuilder csv = new StringBuilder("studentId,firstName,lastName,dob,class,score\n");
        for (int i = 1; i <= 1000; i++) {
            csv.append(String.format("%d,First%d,Last%d,2005-06-15,Class%d,%d%n",
                    i, i, i, (i % 5) + 1, 60 + (i % 20)));
        }

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.csv",
                "text/csv",
                csv.toString().getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/upload")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inserted", is(1000)));

        assertEquals(1000, studentRepository.count());
    }

    @Test
    void uploadCsv_shouldSkipInvalidRows() throws Exception {
        String csvContent = """
                studentId,firstName,lastName,dob,class,score
                1,John,Doe,2005-06-15,Class1,70
                invalid,row
                3,Bob,Wilson,2005-11-10,Class1,80
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "students.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/upload")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inserted", is(2)));
    }

    @Test
    void uploadCsv_shouldHandleEmptyFile() throws Exception {
        String csvContent = "studentId,firstName,lastName,dob,class,score\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/upload")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inserted", is(0)));
    }
}
