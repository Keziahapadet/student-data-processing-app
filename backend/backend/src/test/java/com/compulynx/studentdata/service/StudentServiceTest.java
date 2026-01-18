package com.compulynx.studentdata.service;

import com.compulynx.studentdata.model.Student;
import com.compulynx.studentdata.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    private StudentService studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentService(studentRepository);
    }

    @Test
    void getStudents_shouldReturnPagedResults() {
        List<Student> students = Arrays.asList(
                createStudent(1L, "John", "Doe", "Class1", 75),
                createStudent(2L, "Jane", "Smith", "Class2", 80)
        );
        Page<Student> expectedPage = new PageImpl<>(students, PageRequest.of(0, 10), 2);

        when(studentRepository.search(eq(null), eq(null), any(Pageable.class)))
                .thenReturn(expectedPage);

        Page<Student> result = studentService.getStudents(null, null, 0, 10);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        verify(studentRepository).search(null, null, PageRequest.of(0, 10));
    }

    @Test
    void getStudents_withStudentIdFilter_shouldFilterByStudentId() {
        Student student = createStudent(1L, "John", "Doe", "Class1", 75);
        Page<Student> expectedPage = new PageImpl<>(Collections.singletonList(student));

        when(studentRepository.search(eq(1L), eq(null), any(Pageable.class)))
                .thenReturn(expectedPage);

        Page<Student> result = studentService.getStudents(1L, null, 0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getStudentId());
        verify(studentRepository).search(eq(1L), eq(null), any(Pageable.class));
    }

    @Test
    void getStudents_withClassFilter_shouldFilterByClass() {
        List<Student> class1Students = Arrays.asList(
                createStudent(1L, "John", "Doe", "Class1", 75),
                createStudent(3L, "Bob", "Wilson", "Class1", 82)
        );
        Page<Student> expectedPage = new PageImpl<>(class1Students);

        when(studentRepository.search(eq(null), eq("Class1"), any(Pageable.class)))
                .thenReturn(expectedPage);

        Page<Student> result = studentService.getStudents(null, "Class1", 0, 10);

        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream()
                .allMatch(s -> "Class1".equals(s.getStudentClass())));
        verify(studentRepository).search(eq(null), eq("Class1"), any(Pageable.class));
    }

    @Test
    void getStudents_withBothFilters_shouldApplyBothFilters() {
        Student student = createStudent(1L, "John", "Doe", "Class1", 75);
        Page<Student> expectedPage = new PageImpl<>(Collections.singletonList(student));

        when(studentRepository.search(eq(1L), eq("Class1"), any(Pageable.class)))
                .thenReturn(expectedPage);

        Page<Student> result = studentService.getStudents(1L, "Class1", 0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getStudentId());
        assertEquals("Class1", result.getContent().get(0).getStudentClass());
    }

    @Test
    void getStudents_withPagination_shouldReturnCorrectPage() {
        Page<Student> expectedPage = new PageImpl<>(
                Collections.emptyList(),
                PageRequest.of(2, 20),
                100
        );

        when(studentRepository.search(any(), any(), any(Pageable.class)))
                .thenReturn(expectedPage);

        Page<Student> result = studentService.getStudents(null, null, 2, 20);

        assertEquals(2, result.getNumber());
        assertEquals(20, result.getSize());
        assertEquals(100, result.getTotalElements());
    }

    @Test
    void getStudents_withNoResults_shouldReturnEmptyPage() {
        Page<Student> emptyPage = new PageImpl<>(Collections.emptyList());

        when(studentRepository.search(any(), any(), any(Pageable.class)))
                .thenReturn(emptyPage);

        Page<Student> result = studentService.getStudents(999L, null, 0, 10);

        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    private Student createStudent(Long id, String firstName, String lastName, String studentClass, int score) {
        return new Student(id, firstName, lastName, LocalDate.of(2005, 6, 15), studentClass, score);
    }
}
