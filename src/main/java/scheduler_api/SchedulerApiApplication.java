package scheduler_api; // 'com.academic' kısmını sildik

import scheduler_api.implementations.DAGCourseGraph;
import scheduler_api.implementations.FastInstructorManager;
import scheduler_api.implementations.FastRoomManager;
import scheduler_api.implementations.KahnScheduler;
import scheduler_api.models.ScheduledCourse;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/schedule")
@CrossOrigin(origins = "*")
public class SchedulerController {

    @GetMapping("/generate") 
    public List<ScheduledCourse> generateSchedule() {
        int totalCourses = 5;       
        int totalInstructors = 3;   
        int totalRooms = 2;         
        int totalTimeSlots = 40;    
        int[] roomCapacities = {30, 50}; 
        int[] courseInstructors = {0, 1, 0, 2, 1}; 
        int[] courseStudentCounts = {20, 45, 25, 40, 10};

        FastInstructorManager instructorManager = new FastInstructorManager();
        instructorManager.initialize(totalInstructors, totalTimeSlots);

        FastRoomManager roomManager = new FastRoomManager();
        roomManager.initialize(totalRooms, totalTimeSlots, roomCapacities);

        DAGCourseGraph courseGraph = new DAGCourseGraph();
        courseGraph.initialize(totalCourses);
        courseGraph.addPrerequisite(0, 2);
        courseGraph.addPrerequisite(1, 3);
        courseGraph.addPrerequisite(2, 4);

        KahnScheduler scheduler = new KahnScheduler();
        scheduler.setManagers(instructorManager, roomManager, courseGraph);
        scheduler.setCourseData(courseInstructors, courseStudentCounts, totalRooms, totalTimeSlots, totalCourses);

        return scheduler.generateSchedule(); 
    }
}