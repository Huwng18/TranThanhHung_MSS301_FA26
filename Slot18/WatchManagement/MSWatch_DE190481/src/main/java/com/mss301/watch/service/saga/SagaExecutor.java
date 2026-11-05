package com.mss301.watch.service.saga;

import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// [TODO-6.9] Execute without an enclosing database transaction.
@Component
@RequiredArgsConstructor
@Slf4j
public class SagaExecutor {
    private final SagaTracker tracker;
    private final SagaFaultInjector faultInjector;
    private final SagaCompensator compensator;
    private final SagaFailureClassifier failureClassifier;
    public void execute(String sagaId, List<SagaStep> steps, Integer failAfterStep) {
        tracker.updateStatus(sagaId, SagaStatus.EXECUTING);
        Deque<CompletedStep> completed = new ArrayDeque<>();   // STACK các bước đã commit
        boolean pivotCommitted = false;
        int stepNo = 0;
        String currentStep = null;

        try {
            for (SagaStep step : steps) {
                stepNo++;
                currentStep = step.name();
                tracker.currentStep(sagaId, step.name());

                step.action().run();                           // 1 local transaction (ở DBWatch hoặc DBBrand)

                if (step.isPivot()) pivotCommitted = true;
                else completed.push(new CompletedStep(stepNo, step));
                step.afterCommit().run();
                tracker.log(sagaId, stepNo, step.name(), "EXECUTE", "SUCCESS",
                    step.isPivot() ? "Pivot committed - saga can only move forward now" : "Local transaction committed");
                log.info("[SAGA {}] step {} {} -> SUCCESS", sagaId, stepNo, step.name());

                if (faultInjector.shouldFailAfter(failAfterStep, stepNo)) {
                    if (step.isPivot()) {
                        // Sau pivot không còn gì để quay lui -> bỏ qua lỗi giả, Saga phải đi tới COMPLETED
                        tracker.log(sagaId, stepNo, "FAULT_INJECTION", "FAULT", "SKIPPED",
                            "Ignored: step " + stepNo + " (" + step.name() + ") is the pivot, saga cannot be rolled back");
                    } else {
                        currentStep = "FAULT_INJECTION after " + step.name();
                        throw new SimulatedSagaFailureException(
                            "Simulated failure after step " + stepNo + " (" + step.name() + ")");
                    }
                }
            }

            tracker.updateStatus(sagaId, SagaStatus.COMPLETED);
            log.info("[SAGA {}] COMPLETED", sagaId);

        } catch (RuntimeException ex) {
            if (pivotCommitted) {
                // Never restore the remote link after an irreversible local delete.
                tracker.updateStatus(sagaId, SagaStatus.COMPLETED);
                log.error("[SAGA {}] post-pivot tracking failed", sagaId, ex);
                return;
            }
            String reason = failureClassifier.describe(ex);
            if (ex instanceof SimulatedSagaFailureException) {
                tracker.log(sagaId, stepNo, "FAULT_INJECTION", "FAULT", "TRIGGERED", reason);
            } else {
                tracker.log(sagaId, stepNo, currentStep, "EXECUTE", "FAILED", reason);
            }
            log.warn("[SAGA {}] FAILED at {}: {}", sagaId, currentStep, reason);
            tracker.fail(sagaId, reason);                                          // FAILED

            boolean allCompensated = compensator.compensate(sagaId, completed);                // COMPENSATING
            SagaStatus finalStatus = allCompensated ? SagaStatus.COMPENSATED : SagaStatus.COMPENSATING;
            tracker.updateStatus(sagaId, finalStatus);
            log.warn("[SAGA {}] finished with status {}", sagaId, finalStatus);

            throw new SagaExecutionException(sagaId, finalStatus, currentStep, failureClassifier.httpStatusFor(ex), reason, ex);
        }
    }

}
