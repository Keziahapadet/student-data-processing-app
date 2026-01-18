package com.compulynx.studentdata.repository;

import com.compulynx.studentdata.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query("SELECT s FROM Student s WHERE (:studentId IS NULL OR s.studentId = :studentId) AND (:studentClass IS NULL OR s.studentClass = :studentClass)")
    Page<Student> search(@Param("studentId") Long studentId, @Param("studentClass") String studentClass, Pageable pageable);
}
