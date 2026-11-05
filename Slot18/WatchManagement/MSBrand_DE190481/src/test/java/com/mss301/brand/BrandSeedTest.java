package com.mss301.brand;

import com.mss301.brand.repository.BrandWatchRepository;
import com.mss301.brand.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

// [TODO-5.6] Seed data matches the DOCX and never recreates deleted links on restart.
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:brand-tests;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class BrandSeedTest {
    @Autowired BrandService service;
    @Autowired BrandWatchRepository links;
    @Autowired DataInitializer initializer;

    @Test
    void initialCountsMatchTenWatchesInTheAssignment() {
        assertEquals(List.of(2L, 2L, 2L, 3L, 1L), service.findAll().stream()
            .map(brand -> brand.watchCount()).toList());
        assertEquals(List.of(4, 9, 10), service.watchesOf(4).watchIds());
    }

    @Test
    void restartingWithNoLinksDoesNotResurrectDeletedWatches() {
        links.deleteAll();
        links.flush();
        initializer.run();
        assertEquals(0, links.count());
    }
}
