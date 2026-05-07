package com.academic.scheduler_api.repository;

import com.academic.scheduler_api.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByDepartmentId(Long departmentId);
}
