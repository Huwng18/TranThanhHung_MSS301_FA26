package com.mss301.watch.service.saga;

import com.mss301.watch.WatchIntegrationTest;
import com.mss301.watch.repository.SagaLogRepository;
import com.mss301.watch.repository.SagaStateRepository;
import com.mss301.watch.repository.WatchRepository;
import com.mss301.watch.service.WatchData;
import com.mss301.watch.service.WatchLocalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

// [TODO-6.4] State and logs survive a caller's local transaction rollback.
@WatchIntegrationTest
class SagaTrackerIntegrationTest {
    @Autowired SagaTracker tracker;
    @Autowired SagaStateRepository states;
    @Autowired SagaLogRepository logs;
    @Autowired WatchLocalService local;
    @Autowired WatchRepository watches;
    @Autowired PlatformTransactionManager transactions;

    @Test
    void requiresNewCommitsStateAndLogIndependentlyOfTheCaller() {
        AtomicReference<String> sagaId = new AtomicReference<>();
        AtomicReference<Integer> watchId = new AtomicReference<>();
        assertThrows(IllegalStateException.class, () ->
            new TransactionTemplate(transactions).executeWithoutResult(status -> {
                sagaId.set(tracker.start("CREATE_WATCH", null, "{}").getSagaId());
                watchId.set(local.insert(new WatchData(null, "Rolled back watch name", 1, 1,
                    "Steel", "Leather", "50 meters", new BigDecimal("125.00"), LocalDate.now(), 10)));
                tracker.log(sagaId.get(), 1, "CREATE_WATCH", "EXECUTE", "SUCCESS", "Independent log");
                throw new IllegalStateException("Rollback caller transaction");
            }));
        assertTrue(states.findBySagaId(sagaId.get()).isPresent());
        assertEquals(1, logs.findBySagaIdOrderByIdAsc(sagaId.get()).size());
        assertFalse(watches.existsById(watchId.get()));
    }
}
