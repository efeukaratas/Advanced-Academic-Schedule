package com.academic.scheduler_api.repository;

import com.academic.scheduler_api.models.EnrollmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<EnrollmentRequest, Long> {

    List<EnrollmentRequest> findByCourseId(Long courseId);

    List<EnrollmentRequest> findByStudentId(Long studentId);

    // Her ders için kayıtlı öğrenci sayısı
    @Query("SELECT e.course.id, COUNT(e) FROM EnrollmentRequest e GROUP BY e.course.id")
    List<Object[]> countEnrollmentsPerCourse();

    // Belirli bir derse kayıtlı öğrenci sayısı
    @Query("SELECT COUNT(e) FROM EnrollmentRequest e WHERE e.course.id = :courseId")
    long countByCourseId(@Param("courseId") Long courseId);
}
