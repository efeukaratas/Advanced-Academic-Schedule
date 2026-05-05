package com.academic.scheduler_api;

// Doğru yollardan import ediyoruz
import com.academic.scheduler_api.implementations.DAGCourseGraph;
import com.academic.scheduler_api.implementations.FastInstructorManager;
import com.academic.scheduler_api.implementations.FastRoomManager;
import com.academic.scheduler_api.implementations.KahnScheduler;
import com.academic.scheduler_api.models.ScheduledCourse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedule")
@CrossOrigin(origins = "*")
public class SchedulerController {

    @GetMapping("/generate") 
    public List<ScheduledCourse> generateSchedule() {
        // 1. Test verileri (Main.java'daki ile aynı)
        int totalCourses = 5;       
        int totalInstructors = 3;   
        int totalRooms = 2;         
        int totalTimeSlots = 40;    
        int[] roomCapacities = {30, 50}; 
        int[] courseInstructors = {0, 1, 0, 2, 1}; 
        int[] courseStudentCounts = {20, 45, 25, 40, 10};

        // 2. Modüllerin başlatılması
        FastInstructorManager instructorManager = new FastInstructorManager();
        instructorManager.initialize(totalInstructors, totalTimeSlots);

        FastRoomManager roomManager = new FastRoomManager();
        roomManager.initialize(totalRooms, totalTimeSlots, roomCapacities);

        DAGCourseGraph courseGraph = new DAGCourseGraph();
        courseGraph.initialize(totalCourses);
        courseGraph.addPrerequisite(0, 2);
        courseGraph.addPrerequisite(1, 3);
        courseGraph.addPrerequisite(2, 4);

        // 3. Çizelgeleyicinin kurulumu
        KahnScheduler scheduler = new KahnScheduler();
        scheduler.setManagers(instructorManager, roomManager, courseGraph);
        scheduler.setCourseData(courseInstructors, courseStudentCounts, totalRooms, totalTimeSlots, totalCourses);

        // 4. SONUÇ DÖNDÜRME (Hatanın çözümü burası)
        return scheduler.generateSchedule(); 
    }
}