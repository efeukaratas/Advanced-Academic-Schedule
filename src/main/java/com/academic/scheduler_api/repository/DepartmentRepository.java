package com.academic.scheduler_api.repository;

import com.academic.scheduler_api.models.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Department findByCode(String code);
}
