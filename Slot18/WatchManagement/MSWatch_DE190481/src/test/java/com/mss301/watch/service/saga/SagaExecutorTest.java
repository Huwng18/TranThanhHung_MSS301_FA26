package com.mss301.watch.service.saga;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.json.JsonMapper;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// [TODO-6.9] A committed action must be eligible for compensation even when logging fails.
class SagaExecutorTest {
    private SagaTracker tracker;
    private SagaExecutor executor;

    @BeforeEach
    void setUp() {
        tracker = mock(SagaTracker.class);
        SagaFailureClassifier classifier = new SagaFailureClassifier(JsonMapper.builder().build());
        SagaCompensator compensator = new SagaCompensator(tracker, classifier);
        ReflectionTestUtils.setField(compensator, "backoffMs", 0L);
        SagaFaultInjector injector = new SagaFaultInjector();
        ReflectionTestUtils.setField(injector, "enabled", true);
        executor = new SagaExecutor(tracker, injector, compensator, classifier);
    }

    @Test
    void completedActionsAreCompensatedInReverseOrder() {
        List<String> events = new ArrayList<>();
        List<SagaStep> steps = List.of(
            SagaStep.of("LOCAL", () -> events.add("local"), "DELETE_LOCAL", () -> events.add("delete-local")),
            SagaStep.of("REMOTE", () -> events.add("remote"), "UNASSIGN", () -> events.add("unassign")));
        var failure = assertThrows(SagaExecutionException.class,
            () -> executor.execute("saga", steps, 2));
        assertEquals(List.of("local", "remote", "unassign", "delete-local"), events);
        assertEquals(SagaStatus.COMPENSATED, failure.getSagaStatus());
    }

    @Test
    void loggingFailureAfterCommitStillCompensatesTheCommittedAction() {
        List<String> events = new ArrayList<>();
        doThrow(new IllegalStateException("logging unavailable"))
            .when(tracker).log(eq("saga"), eq(1), eq("LOCAL"), eq("EXECUTE"), eq("SUCCESS"), anyString());
        assertThrows(SagaExecutionException.class, () -> executor.execute("saga", List.of(
            SagaStep.of("LOCAL", () -> events.add("insert"), "DELETE", () -> events.add("delete"))), null));
        assertEquals(List.of("insert", "delete"), events);
    }

    @Test
    void trackingCallbackFailureAfterCommitStillCompensatesTheCommittedAction() {
        List<String> events = new ArrayList<>();
        SagaStep step = SagaStep.of("LOCAL", () -> events.add("insert"), "DELETE", () -> events.add("delete"))
            .withAfterCommit(() -> { throw new IllegalStateException("state write failed"); });
        assertThrows(SagaExecutionException.class,
            () -> executor.execute("saga", List.of(step), null));
        assertEquals(List.of("insert", "delete"), events);
    }

    @Test
    void trackingFailureAfterPivotNeverRestoresTheRemoteLink() {
        List<String> events = new ArrayList<>();
        doThrow(new IllegalStateException("tracking unavailable"))
            .when(tracker).log(eq("saga"), eq(2), eq("DELETE"), eq("EXECUTE"), eq("SUCCESS"), anyString());
        executor.execute("saga", List.of(
            SagaStep.of("UNASSIGN", () -> events.add("unassign"), "REASSIGN", () -> events.add("reassign")),
            SagaStep.pivot("DELETE", () -> events.add("delete"))), null);
        assertEquals(List.of("unassign", "delete"), events);
        verify(tracker).updateStatus("saga", SagaStatus.COMPLETED);
        verify(tracker, never()).updateStatus("saga", SagaStatus.COMPENSATING);
    }
}
