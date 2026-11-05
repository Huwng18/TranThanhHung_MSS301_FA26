package com.mss301.brand;

import com.mss301.brand.repository.BrandWatchRepository;
import com.mss301.brand.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

// [TODO-5.2] Check idempotency against an actual database and its unique constraint.
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:brand-tests;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class BrandAssignmentTest {
    @Autowired BrandService service;
    @Autowired BrandWatchRepository links;

    @Test
    void repeatAssignmentDoesNotCreateDuplicateLinks() {
        long initialCount = links.count();
        service.assignWatch(1, 101);
        service.assignWatch(1, 101);
        links.flush();
        assertEquals(initialCount + 1, links.count());
        assertEquals(1, links.findByWatchId(101).orElseThrow().getBrandId());
    }

    @Test
    void changingBrandMovesTheExistingLink() {
        long initialCount = links.count();
        Long linkId = links.findByWatchId(1).orElseThrow().getId();
        var result = service.assignWatch(2, 1);
        links.flush();
        assertEquals(1, result.previousBrandId());
        assertEquals(initialCount, links.count());
        assertEquals(linkId, links.findByWatchId(1).orElseThrow().getId());
        assertEquals(2, links.findByWatchId(1).orElseThrow().getBrandId());
    }

    @Test
    void unknownBrandIsRejectedBeforeAnyLinkIsChanged() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
            () -> service.assignWatch(999, 1));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        assertEquals(1, links.findByWatchId(1).orElseThrow().getBrandId());
    }
}
