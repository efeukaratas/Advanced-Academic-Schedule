package com.academic.scheduler_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SchedulerApiApplicationTests {

    @Test
    void contextLoads() {
        // Context yüklenirse ve DataInitializer çalışmazsa test geçer
    }
}
