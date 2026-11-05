package com.mss301.brand;

import com.mss301.brand.repository.BrandWatchRepository;
import com.mss301.brand.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

// [TODO-5.3] Repeated compensation must not fail or remove another watch's link.
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:brand-tests;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class BrandUnassignmentTest {
    @Autowired BrandService service;
    @Autowired BrandWatchRepository links;

    @Test
    void repeatedUnassignmentIsANoOp() {
        long initialCount = links.count();
        assertEquals("UNASSIGNED", service.unassignWatch(1).status());
        links.flush();
        assertEquals("NOTHING_TO_DO", service.unassignWatch(1).status());
        assertEquals(initialCount - 1, links.count());
        assertTrue(links.findByWatchId(2).isPresent());
    }
}
