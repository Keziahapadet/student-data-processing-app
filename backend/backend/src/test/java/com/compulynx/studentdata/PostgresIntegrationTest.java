package com.compulynx.studentdata;

import com.compulynx.studentdata.model.Student;
import com.compulynx.studentdata.repository.StudentRepository;
import com.compulynx.studentdata.service.StudentUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class PostgresIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentUploadService uploadService;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
    }

    @Test
    void shouldConnectToPostgres() {
        assertTrue(postgres.isRunning());
    }

    @Test
    void shouldSaveAndRetrieveStudent() {
        Student student = new Student(1L, "John", "Doe", LocalDate.of(2005, 6, 15), "Class1", 75);

        studentRepository.save(student);

        Optional<Student> found = studentRepository.findById(1L);
        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
        assertEquals("Doe", found.get().getLastName());
        assertEquals(75, found.get().getScore());
    }

    @Test
    void shouldUploadCsvUsingCopyCommand() throws Exception {
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

        int inserted = uploadService.uploadCsv(file);

        assertEquals(3, inserted);
        assertEquals(3, studentRepository.count());

        // Verify score transformation (+5)
        Optional<Student> student = studentRepository.findById(1L);
        assertTrue(student.isPresent());
        assertEquals(75, student.get().getScore()); // 70 + 5 = 75
    }

    @Test
    void shouldHandleLargeUploadWithCopyCommand() throws Exception {
        StringBuilder csv = new StringBuilder("studentId,firstName,lastName,dob,class,score\n");
        int recordCount = 10000;

        for (int i = 1; i <= recordCount; i++) {
            csv.append(String.format("%d,First%d,Last%d,2005-06-15,Class%d,%d%n",
                    i, i, i, (i % 5) + 1, 60 + (i % 20)));
        }

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.csv",
                "text/csv",
                csv.toString().getBytes(StandardCharsets.UTF_8)
        );

        long startTime = System.currentTimeMillis();
        int inserted = uploadService.uploadCsv(file);
        long duration = System.currentTimeMillis() - startTime;

        assertEquals(recordCount, inserted);
        assertEquals(recordCount, studentRepository.count());

        // Performance assertion - COPY command should be fast
        assertTrue(duration < 10000, "Upload of " + recordCount + " records should complete within 10 seconds");
    }

    @Test
    void shouldSearchStudentsWithFilters() {
        studentRepository.save(new Student(1L, "John", "Doe", LocalDate.of(2005, 6, 15), "Class1", 75));
        studentRepository.save(new Student(2L, "Jane", "Smith", LocalDate.of(2006, 3, 20), "Class2", 80));
        studentRepository.save(new Student(3L, "Bob", "Wilson", LocalDate.of(2005, 11, 10), "Class1", 85));

        var result = studentRepository.search(null, "Class1", org.springframework.data.domain.PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(s -> "Class1".equals(s.getStudentClass())));
    }
}
