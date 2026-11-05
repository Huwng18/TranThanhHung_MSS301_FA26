package com.mss301.watch.service.saga;

import java.util.Deque;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// [TODO-6.10] Retry in LIFO order and stop if an inverse transaction fails.
@Component
@RequiredArgsConstructor
@Slf4j
public class SagaCompensator {
    private final SagaTracker tracker;
    private final SagaFailureClassifier failureClassifier;
    @Value("${saga.compensation.max-attempts:3}")
    private int maxAttempts = 3;
    @Value("${saga.compensation.backoff-ms:1000}")
    private long backoffMs = 1000;
    public boolean compensate(String sagaId, Deque<CompletedStep> completed) {
        tracker.updateStatus(sagaId, SagaStatus.COMPENSATING);
        while (!completed.isEmpty()) {
            CompletedStep done = completed.pop();                                  // LIFO
            if (!compensateWithRetry(sagaId, done)) {
                tracker.log(sagaId, done.stepNo(), "COMPENSATION_HALTED", "COMPENSATE", "FAILED",
                    completed.size() + " earlier step(s) still need compensation - manual intervention required");
                log.error("[SAGA {}] compensation halted at step {}, saga stays COMPENSATING", sagaId, done.stepNo());
                return false;
            }
        }
        return true;
    }

    /** Compensation phải "Reliable" (Slide 25): thử lại vài lần. Nhờ IDEMPOTENT nên retry an toàn. */
    // [TODO-6.10: retry compensation]
    private boolean compensateWithRetry(String sagaId, CompletedStep done) {
        SagaStep step = done.step();
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                step.compensation().run();
                tracker.log(sagaId, done.stepNo(), step.compensationName(), "COMPENSATE", "SUCCESS",
                    "Undo " + step.name() + " (attempt " + attempt + ")");
                log.info("[SAGA {}] compensate step {} {} -> SUCCESS", sagaId, done.stepNo(), step.compensationName());
                return true;
            } catch (RuntimeException ex) {
                tracker.log(sagaId, done.stepNo(), step.compensationName(), "COMPENSATE", "FAILED",
                    "Attempt " + attempt + "/" + maxAttempts + ": " + failureClassifier.describe(ex));
                if (attempt < maxAttempts && !pause()) return false;
            }
        }
        return false;
    }


    private boolean pause() {
        try {
            Thread.sleep(backoffMs);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
