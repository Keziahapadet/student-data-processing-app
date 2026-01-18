package com.compulynx.studentdata.repository;

import com.compulynx.studentdata.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
    }

    @Test
    void search_shouldReturnAllStudents_whenNoFiltersApplied() {
        List<Student> students = createTestStudents(10);
        studentRepository.saveAll(students);

        Page<Student> result = studentRepository.search(null, null, PageRequest.of(0, 20));

        assertEquals(10, result.getTotalElements());
    }

    @Test
    void search_shouldFilterByStudentId() {
        List<Student> students = createTestStudents(10);
        studentRepository.saveAll(students);

        Page<Student> result = studentRepository.search(5L, null, PageRequest.of(0, 20));

        assertEquals(1, result.getTotalElements());
        assertEquals(5L, result.getContent().get(0).getStudentId());
    }

    @Test
    void search_shouldFilterByStudentClass() {
        List<Student> students = createTestStudents(10);
        studentRepository.saveAll(students);

        Page<Student> result = studentRepository.search(null, "Class1", PageRequest.of(0, 20));

        assertTrue(result.getTotalElements() > 0);
        assertTrue(result.getContent().stream()
                .allMatch(s -> "Class1".equals(s.getStudentClass())));
    }

    @Test
    void search_shouldFilterByBothStudentIdAndClass() {
        studentRepository.save(new Student(1L, "John", "Doe", LocalDate.of(2005, 6, 15), "Class1", 75));
        studentRepository.save(new Student(2L, "Jane", "Smith", LocalDate.of(2006, 3, 20), "Class2", 80));

        Page<Student> result = studentRepository.search(1L, "Class1", PageRequest.of(0, 20));

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getStudentId());
        assertEquals("Class1", result.getContent().get(0).getStudentClass());
    }

    @Test
    void search_shouldReturnEmpty_whenStudentIdNotFound() {
        List<Student> students = createTestStudents(10);
        studentRepository.saveAll(students);

        Page<Student> result = studentRepository.search(999L, null, PageRequest.of(0, 20));

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void search_shouldReturnEmpty_whenClassNotFound() {
        List<Student> students = createTestStudents(10);
        studentRepository.saveAll(students);

        Page<Student> result = studentRepository.search(null, "NonExistentClass", PageRequest.of(0, 20));

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void search_shouldSupportPagination() {
        List<Student> students = createTestStudents(25);
        studentRepository.saveAll(students);

        Page<Student> page0 = studentRepository.search(null, null, PageRequest.of(0, 10));
        Page<Student> page1 = studentRepository.search(null, null, PageRequest.of(1, 10));
        Page<Student> page2 = studentRepository.search(null, null, PageRequest.of(2, 10));

        assertEquals(10, page0.getContent().size());
        assertEquals(10, page1.getContent().size());
        assertEquals(5, page2.getContent().size());
        assertEquals(25, page0.getTotalElements());
        assertEquals(3, page0.getTotalPages());
    }

    @Test
    void save_shouldPersistStudent() {
        Student student = new Student(1L, "John", "Doe", LocalDate.of(2005, 6, 15), "Class1", 75);

        Student saved = studentRepository.save(student);

        assertNotNull(saved);
        assertEquals(1L, saved.getStudentId());
        assertEquals("John", saved.getFirstName());
    }

    @Test
    void findById_shouldReturnStudent_whenExists() {
        Student student = new Student(1L, "John", "Doe", LocalDate.of(2005, 6, 15), "Class1", 75);
        studentRepository.save(student);

        assertTrue(studentRepository.findById(1L).isPresent());
        assertEquals("John", studentRepository.findById(1L).get().getFirstName());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        assertTrue(studentRepository.findById(999L).isEmpty());
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
