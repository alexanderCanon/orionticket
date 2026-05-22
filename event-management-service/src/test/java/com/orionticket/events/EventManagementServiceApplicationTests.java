package com.orionticket.events;

import com.orionticket.events.infrastructure.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class EventManagementServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
