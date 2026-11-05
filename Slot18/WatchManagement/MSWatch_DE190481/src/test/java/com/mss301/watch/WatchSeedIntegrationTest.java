package com.mss301.watch;

import com.mss301.watch.repository.WatchCategoryRepository;
import com.mss301.watch.repository.WatchRepository;
import com.mss301.watch.service.WatchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

// [TODO-6.16] Keep historical seed exceptions and prevent resurrection on restart.
@WatchIntegrationTest
@Transactional
class WatchSeedIntegrationTest {
    @Autowired WatchRepository watches;
    @Autowired WatchCategoryRepository categories;
    @Autowired WatchService service;
    @Autowired DataInitializer initializer;

    @Test
    void exactSeedAndPublicListIncludeCategoryNamesInNewestFirstOrder() {
        assertEquals(10, watches.count());
        assertEquals(5, categories.count());
        assertEquals(new BigDecimal("19.99"), watches.findById(9).orElseThrow().getPrice());
        assertEquals("IWC Pilot's Watch Mark XVIII", watches.findById(10).orElseThrow().getName());
        assertEquals(List.of(10, 9, 8, 7, 6, 5, 4, 3, 2, 1), service.findAll().stream()
            .map(watch -> watch.getWatchId()).toList());
        assertTrue(service.findAll().stream().allMatch(watch -> watch.getCategoryName() != null));
    }

    @Test
    void restartingAfterAllWatchesAreDeletedDoesNotRecreateThem() {
        watches.deleteAll();
        watches.flush();
        initializer.run();
        assertEquals(0, watches.count());
        assertEquals(5, categories.count());
    }
}
