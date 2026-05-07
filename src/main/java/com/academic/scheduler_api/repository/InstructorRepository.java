package com.academic.scheduler_api.repository;

import com.academic.scheduler_api.models.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Long> {
    List<Instructor> findByDepartmentId(Long departmentId);
}
