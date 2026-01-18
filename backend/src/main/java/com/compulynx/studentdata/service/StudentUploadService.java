package com.compulynx.studentdata.service;

import com.compulynx.studentdata.model.Student;
import com.compulynx.studentdata.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class StudentUploadService {

    private final StudentRepository studentRepository;

    public StudentUploadService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional
    public int uploadCsv(MultipartFile file) throws IOException {
        int inserted = 0;
        List<Student> buffer = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean headerSkipped = false;
            while ((line = reader.readLine()) != null) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }
                String[] tokens = line.split(",");
                if (tokens.length < 6) {
                    continue;
                }
                Student student = parseStudent(tokens);
                buffer.add(student);
                if (buffer.size() >= 1000) {
                    studentRepository.saveAll(buffer);
                    inserted += buffer.size();
                    buffer.clear();
                }
            }
            if (!buffer.isEmpty()) {
                studentRepository.saveAll(buffer);
                inserted += buffer.size();
            }
        }
        return inserted;
    }

    private Student parseStudent(String[] tokens) {
        Long studentId = Long.parseLong(tokens[0].trim());
        String firstName = tokens[1].trim();
        String lastName = tokens[2].trim();
        LocalDate dob = LocalDate.parse(tokens[3].trim());
        String studentClass = tokens[4].trim();
        int baseScore = Integer.parseInt(tokens[5].trim());
        int transformedScore = baseScore + 5;
        return new Student(studentId, firstName, lastName, dob, studentClass, transformedScore);
    }
}
