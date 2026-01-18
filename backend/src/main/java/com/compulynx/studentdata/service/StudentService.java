package com.compulynx.studentdata.service;

import com.compulynx.studentdata.model.Student;
import com.compulynx.studentdata.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Page<Student> getStudents(Long studentId, String studentClass, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return studentRepository.search(studentId, studentClass, pageable);
    }
}
