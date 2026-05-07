package com.academic.scheduler_api;

import com.academic.scheduler_api.models.*;
import com.academic.scheduler_api.repository.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Uygulama tamamen hazır olduğunda (ApplicationReadyEvent) H2 DB'ye örnek veri yükler.
 * Gerçek sistemde bu veriler kurumsal DB'den gelecektir.
 * Test profilinde çalışmaz (@Profile("!test")).
 */
@Component
@Profile("!test")
public class DataInitializer {

    private final DepartmentRepository  deptRepo;
    private final CourseRepository      courseRepo;
    private final StudentRepository     studentRepo;
    private final EnrollmentRepository  enrollRepo;
    private final FacultyPreferenceRepository prefRepo;

    public DataInitializer(DepartmentRepository deptRepo,
                           CourseRepository courseRepo,
                           StudentRepository studentRepo,
                           EnrollmentRepository enrollRepo,
                           FacultyPreferenceRepository prefRepo) {
        this.deptRepo    = deptRepo;
        this.courseRepo  = courseRepo;
        this.studentRepo = studentRepo;
        this.enrollRepo  = enrollRepo;
        this.prefRepo    = prefRepo;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void load() {

        // ── 1. Departmanlar ─────────────────────────────────────────────────
        Department cs   = deptRepo.save(new Department("Bilgisayar Mühendisliği", "CS",   "A Blok"));
        Department math = deptRepo.save(new Department("Matematik",               "MATH", "B Blok"));
        Department phy  = deptRepo.save(new Department("Fizik",                   "PHY",  "C Blok"));

        // ── 2. Dersler ──────────────────────────────────────────────────────
        Course cs101 = courseRepo.save(new Course("Algoritmalara Giriş",  "CS101",   3, 0, 25, cs));
        Course cs201 = courseRepo.save(new Course("Veri Yapıları",        "CS201",   3, 1, 30, cs));
        Course cs301 = courseRepo.save(new Course("İşletim Sistemleri",   "CS301",   4, 2, 35, cs));
        Course cs401 = courseRepo.save(new Course("Yapay Zeka",           "CS401",   3, 3, 40, cs));
        Course m101  = courseRepo.save(new Course("Matematik I",          "MATH101", 4, 5, 50, math));
        Course m201  = courseRepo.save(new Course("Lineer Cebir",         "MATH201", 3, 6, 30, math));
        Course p101  = courseRepo.save(new Course("Fizik I",              "PHY101",  4, 8, 60, phy));
        Course p201  = courseRepo.save(new Course("Kuantum Fiziği",       "PHY201",  3, 9, 25, phy));

        // ── 3. Önkoşullar (ArrayList kullan — Hibernate mutable liste ister) ─
        cs201.setPrerequisiteIds(new ArrayList<>(Arrays.asList(cs101.getId())));
        courseRepo.save(cs201);

        cs301.setPrerequisiteIds(new ArrayList<>(Arrays.asList(cs201.getId())));
        courseRepo.save(cs301);

        cs401.setPrerequisiteIds(new ArrayList<>(Arrays.asList(cs301.getId())));
        courseRepo.save(cs401);

        m201.setPrerequisiteIds(new ArrayList<>(Arrays.asList(m101.getId())));
        courseRepo.save(m201);

        p201.setPrerequisiteIds(new ArrayList<>(Arrays.asList(p101.getId(), m101.getId())));
        courseRepo.save(p201);

        // ── 4. Öğrenciler ───────────────────────────────────────────────────
        Student s1 = studentRepo.save(new Student("Ahmet Yılmaz",  "ahmet@uni.edu",  cs,   2));
        Student s2 = studentRepo.save(new Student("Ayşe Kaya",     "ayse@uni.edu",   cs,   2));
        Student s3 = studentRepo.save(new Student("Mehmet Demir",  "mehmet@uni.edu", math, 1));
        Student s4 = studentRepo.save(new Student("Fatma Çelik",   "fatma@uni.edu",  cs,   3));
        Student s5 = studentRepo.save(new Student("Ali Şahin",     "ali@uni.edu",    phy,  1));
        Student s6 = studentRepo.save(new Student("Zeynep Arslan", "zeynep@uni.edu", cs,   1));
        Student s7 = studentRepo.save(new Student("Can Öztürk",    "can@uni.edu",    math, 2));
        Student s8 = studentRepo.save(new Student("Elif Yıldız",   "elif@uni.edu",   phy,  2));

        // ── 5. Kayıt İstekleri ──────────────────────────────────────────────
        enrollRepo.save(new EnrollmentRequest(s1, cs101, 1));
        enrollRepo.save(new EnrollmentRequest(s2, cs101, 1));
        enrollRepo.save(new EnrollmentRequest(s6, cs101, 2));
        enrollRepo.save(new EnrollmentRequest(s4, cs201, 1));
        enrollRepo.save(new EnrollmentRequest(s1, cs201, 2));
        enrollRepo.save(new EnrollmentRequest(s2, cs201, 2));
        enrollRepo.save(new EnrollmentRequest(s4, cs301, 1));
        enrollRepo.save(new EnrollmentRequest(s1, cs401, 1));
        enrollRepo.save(new EnrollmentRequest(s3, m101,  1));
        enrollRepo.save(new EnrollmentRequest(s7, m101,  1));
        enrollRepo.save(new EnrollmentRequest(s6, m101,  2));
        enrollRepo.save(new EnrollmentRequest(s3, m201,  1));
        enrollRepo.save(new EnrollmentRequest(s7, m201,  1));
        enrollRepo.save(new EnrollmentRequest(s5, p101,  1));
        enrollRepo.save(new EnrollmentRequest(s8, p101,  1));
        enrollRepo.save(new EnrollmentRequest(s5, p201,  1));
        enrollRepo.save(new EnrollmentRequest(s8, p201,  1));

        // ── 6. Eğitmen Tercihleri (Faculty Preferences) ─────────────────────
        // Eğitmen 0 (cs101 hocası), Pazartesi 08:00 (slot 0) ve 09:00 (slot 1) ders veremez.
        prefRepo.save(new FacultyPreference(0, 0, false));
        prefRepo.save(new FacultyPreference(0, 1, false));

        System.out.println("✅ Örnek veriler yüklendi: 3 departman, 8 ders, 8 öğrenci, 17 kayıt isteği, 2 eğitmen tercihi.");
    }
}
