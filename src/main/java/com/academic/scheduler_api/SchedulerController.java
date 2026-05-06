package com.academic.scheduler_api;

import com.academic.scheduler_api.implementations.DAGCourseGraph;
import com.academic.scheduler_api.implementations.FastInstructorManager;
import com.academic.scheduler_api.implementations.FastRoomManager;
import com.academic.scheduler_api.implementations.KahnScheduler;
import com.academic.scheduler_api.models.ScheduledCourse;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/schedule")
@CrossOrigin(origins = "*")
public class SchedulerController {

    private final FastInstructorManager instructorManager = new FastInstructorManager();
    private final FastRoomManager       roomManager       = new FastRoomManager();
    private final DAGCourseGraph        courseGraph       = new DAGCourseGraph();
    private final KahnScheduler         scheduler         = new KahnScheduler();

    @GetMapping("/generate")
    public List<ScheduledCourse> generate() {

        // 10 eğitmen, 40 zaman dilimi
        instructorManager.initialize(10, 40);

        // 5 oda, 40 zaman dilimi, her odanın kapasitesi
        int[] capacities = {30, 40, 50, 60, 100};
        roomManager.initialize(5, 40, capacities);

        // 6 ders için DAG kur
        courseGraph.initialize(6);
        courseGraph.addPrerequisite(0, 1); // Ders 1, Ders 0'ı gerektirir
        courseGraph.addPrerequisite(1, 2); // Ders 2, Ders 1'i gerektirir
        courseGraph.addPrerequisite(0, 3); // Ders 3, Ders 0'ı gerektirir

        scheduler.setManagers(instructorManager, roomManager, courseGraph);

        int[] courseIds     = {0, 1, 2, 3, 4, 5};
        int[] instructorIds = {0, 1, 2, 0, 3, 4};
        scheduler.setCourseData(courseIds, instructorIds, 6, 10, 40);

        long start   = System.currentTimeMillis();
        List<ScheduledCourse> result = scheduler.generateSchedule();
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("Çizelge oluşturma süresi: " + elapsed + " ms");

        return result;
    }
}