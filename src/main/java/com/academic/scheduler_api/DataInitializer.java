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
 * Uygulama hazir oldugunda H2 DB'ye ornek veri yukler.
 * Gercek sistemde bu veriler kurumsal DB'den gelecektir.
 */
@Component
@Profile("!test")
public class DataInitializer {

    private final DepartmentRepository        deptRepo;
    private final InstructorRepository        instrRepo;
    private final RoomRepository              roomRepo;
    private final CourseRepository             courseRepo;
    private final StudentRepository            studentRepo;
    private final EnrollmentRepository         enrollRepo;
    private final FacultyPreferenceRepository  prefRepo;

    public DataInitializer(DepartmentRepository deptRepo,
                           InstructorRepository instrRepo,
                           RoomRepository roomRepo,
                           CourseRepository courseRepo,
                           StudentRepository studentRepo,
                           EnrollmentRepository enrollRepo,
                           FacultyPreferenceRepository prefRepo) {
        this.deptRepo    = deptRepo;
        this.instrRepo   = instrRepo;
        this.roomRepo    = roomRepo;
        this.courseRepo  = courseRepo;
        this.studentRepo = studentRepo;
        this.enrollRepo  = enrollRepo;
        this.prefRepo    = prefRepo;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void load() {

        // Persistent DB: zaten veri varsa ekleme
        if (deptRepo.count() > 0) {
            System.out.println("--- DB zaten dolu, ornek veri yuklemesi atlandi. ---");
            return;
        }

        // ── 1. Departmanlar ─────────────────────────────────────────────────
        Department cs   = deptRepo.save(new Department("Bilgisayar Muhendisligi", "CS",   "A Blok"));
        Department math = deptRepo.save(new Department("Matematik",               "MATH", "B Blok"));
        Department phy  = deptRepo.save(new Department("Fizik",                   "PHY",  "C Blok"));

        // ── 2. Egitmenler (Instructor entity) ───────────────────────────────
        Instructor i0 = instrRepo.save(new Instructor("Ahmet Ozturk",     "Prof. Dr.",  "ahmet.oz@uni.edu",    cs));
        Instructor i1 = instrRepo.save(new Instructor("Fatma Yildirim",   "Doc. Dr.",   "fatma.y@uni.edu",     cs));
        Instructor i2 = instrRepo.save(new Instructor("Mehmet Kaya",      "Dr.",        "mehmet.k@uni.edu",    cs));
        Instructor i3 = instrRepo.save(new Instructor("Zeynep Demir",     "Prof. Dr.",  "zeynep.d@uni.edu",    cs));
        Instructor i4 = instrRepo.save(new Instructor("Ali Celik",        "Dr.",        "ali.c@uni.edu",       cs));
        Instructor i5 = instrRepo.save(new Instructor("Ayse Arslan",      "Prof. Dr.",  "ayse.a@uni.edu",      math));
        Instructor i6 = instrRepo.save(new Instructor("Hasan Sahin",      "Doc. Dr.",   "hasan.s@uni.edu",     math));
        Instructor i7 = instrRepo.save(new Instructor("Elif Korkmaz",     "Dr.",        "elif.k@uni.edu",      math));
        Instructor i8 = instrRepo.save(new Instructor("Burak Aydin",      "Prof. Dr.",  "burak.a@uni.edu",     phy));
        Instructor i9 = instrRepo.save(new Instructor("Selin Tas",        "Doc. Dr.",   "selin.t@uni.edu",     phy));

        // ── 3. Odalar (Room entity) ─────────────────────────────────────────
        Room r0 = roomRepo.save(new Room("A-101", "A Blok", 25,  "Sinif"));
        Room r1 = roomRepo.save(new Room("A-102", "A Blok", 30,  "Sinif"));
        Room r2 = roomRepo.save(new Room("B-201", "B Blok", 40,  "Sinif"));
        Room r3 = roomRepo.save(new Room("B-Lab1","B Blok", 50,  "Lab"));
        Room r4 = roomRepo.save(new Room("C-Amfi","C Blok", 60,  "Amfi"));
        Room r5 = roomRepo.save(new Room("C-301", "C Blok", 100, "Amfi"));

        // ── 4. Dersler (Instructor entity baglantili) ───────────────────────
        Course cs101 = courseRepo.save(new Course("Algoritmalara Giris",  "CS101",   3, i0, 25, cs));
        Course cs201 = courseRepo.save(new Course("Veri Yapilari",        "CS201",   3, i1, 30, cs));
        Course cs301 = courseRepo.save(new Course("Isletim Sistemleri",   "CS301",   4, i2, 35, cs));
        Course cs401 = courseRepo.save(new Course("Yapay Zeka",           "CS401",   3, i3, 40, cs));
        Course m101  = courseRepo.save(new Course("Matematik I",          "MATH101", 4, i5, 50, math));
        Course m201  = courseRepo.save(new Course("Lineer Cebir",         "MATH201", 3, i6, 30, math));
        Course p101  = courseRepo.save(new Course("Fizik I",              "PHY101",  4, i8, 60, phy));
        Course p201  = courseRepo.save(new Course("Kuantum Fizigi",       "PHY201",  3, i9, 25, phy));

        // ── 5. Onkosuller ───────────────────────────────────────────────────
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

        // ── 6. Ogrenciler ───────────────────────────────────────────────────
        Student s1 = studentRepo.save(new Student("Ahmet Yilmaz",  "ahmet@uni.edu",  cs,   2));
        Student s2 = studentRepo.save(new Student("Ayse Kaya",     "ayse@uni.edu",   cs,   2));
        Student s3 = studentRepo.save(new Student("Mehmet Demir",  "mehmet@uni.edu", math, 1));
        Student s4 = studentRepo.save(new Student("Fatma Celik",   "fatma@uni.edu",  cs,   3));
        Student s5 = studentRepo.save(new Student("Ali Sahin",     "ali@uni.edu",    phy,  1));
        Student s6 = studentRepo.save(new Student("Zeynep Arslan", "zeynep@uni.edu", cs,   1));
        Student s7 = studentRepo.save(new Student("Can Ozturk",    "can@uni.edu",    math, 2));
        Student s8 = studentRepo.save(new Student("Elif Yildiz",   "elif@uni.edu",   phy,  2));

        // ── 7. Kayit Istekleri ──────────────────────────────────────────────
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

        // ── 8. Egitmen Tercihleri (CSP oncelik puanlari dahil) ─────────────
        // i0 (Prof. Dr. Ahmet Ozturk): Pazartesi sabah yasakli, Sali sabah tercih
        prefRepo.save(new FacultyPreference(i0.getId().intValue(), 0, false, 0));
        prefRepo.save(new FacultyPreference(i0.getId().intValue(), 1, false, 0));
        prefRepo.save(new FacultyPreference(i0.getId().intValue(), 10, true, 3));
        prefRepo.save(new FacultyPreference(i0.getId().intValue(), 11, true, 3));

        // i5 (Prof. Dr. Ayse Arslan): Cuma ogle istenmiyor, Pzt sabah tercih
        prefRepo.save(new FacultyPreference(i5.getId().intValue(), 44, true, 1));
        prefRepo.save(new FacultyPreference(i5.getId().intValue(), 0, true, 3));
        prefRepo.save(new FacultyPreference(i5.getId().intValue(), 1, true, 3));

        // i8 (Prof. Dr. Burak Aydin): Carsamba yasakli
        prefRepo.save(new FacultyPreference(i8.getId().intValue(), 20, false, 0));
        prefRepo.save(new FacultyPreference(i8.getId().intValue(), 21, false, 0));

        System.out.println("--- Veriler yuklendi: 3 dept, 10 egitmen, 6 oda, 8 ders, 8 ogrenci, 17 kayit, 11 tercih ---");
    }
}
