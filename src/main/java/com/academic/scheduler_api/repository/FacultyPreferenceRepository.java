package com.academic.scheduler_api.repository;

import com.academic.scheduler_api.models.FacultyPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacultyPreferenceRepository extends JpaRepository<FacultyPreference, Long> {
    List<FacultyPreference> findByInstructorId(int instructorId);
}
