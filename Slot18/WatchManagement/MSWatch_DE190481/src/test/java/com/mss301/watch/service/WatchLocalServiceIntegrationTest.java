package com.mss301.watch.service;

import com.mss301.watch.WatchIntegrationTest;
import com.mss301.watch.repository.WatchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

// [TODO-6.5] Each service call commits; restoration uses a later local transaction.
@WatchIntegrationTest
class WatchLocalServiceIntegrationTest {
    @Autowired WatchLocalService local;
    @Autowired WatchRepository watches;

    @Test
    void committedUpdateCanBeRestoredFromAnImmutableSnapshot() {
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        Integer id = local.insert(new WatchData(null, "Original watch name", 1, 1,
            "Steel", "Leather", "50 meters", new BigDecimal("125.00"), LocalDate.now(), 20));
        try {
            assertTrue(watches.existsById(id));
            WatchData before = local.load(id);
            WatchData after = new WatchData(id, "Updated watch name", 2, 3,
                "Titanium", "Rubber", "100 meters", new BigDecimal("250.00"), LocalDate.now(), 5);
            local.update(after);
            assertEquals(after, local.load(id));
            local.update(before);
            assertEquals(before, local.load(id));
        } finally {
            local.deleteIfExists(id);
        }
        local.deleteIfExists(id);
        assertFalse(watches.existsById(id));
    }
}
