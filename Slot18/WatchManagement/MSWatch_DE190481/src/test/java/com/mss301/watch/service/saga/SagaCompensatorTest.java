package com.mss301.watch.service.saga;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.json.JsonMapper;
import java.util.ArrayDeque;
import java.util.Deque;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// [TODO-6.10] Retry transient failures, but halt before an earlier local delete if remote undo fails.
class SagaCompensatorTest {
    private SagaTracker tracker;
    private SagaCompensator compensator;

    @BeforeEach
    void setUp() {
        tracker = mock(SagaTracker.class);
        compensator = new SagaCompensator(tracker,
            new SagaFailureClassifier(JsonMapper.builder().build()));
        ReflectionTestUtils.setField(compensator, "backoffMs", 0L);
    }

    @Test
    void transientCompensationFailureCanSucceedOnTheThirdAttempt() {
        Runnable undo = mock(Runnable.class);
        doThrow(new IllegalStateException("down"))
            .doThrow(new IllegalStateException("still down")).doNothing().when(undo).run();
        Deque<CompletedStep> completed = new ArrayDeque<>();
        completed.push(new CompletedStep(1, SagaStep.of("ASSIGN", () -> {}, "UNASSIGN", undo)));
        assertTrue(compensator.compensate("saga", completed));
        verify(undo, times(3)).run();
        verify(tracker, times(2)).log(eq("saga"), eq(1), eq("UNASSIGN"), eq("COMPENSATE"), eq("FAILED"), anyString());
        verify(tracker).log(eq("saga"), eq(1), eq("UNASSIGN"), eq("COMPENSATE"), eq("SUCCESS"), contains("attempt 3"));
    }

    @Test
    void exhaustedRemoteCompensationLeavesEarlierLocalStepUntouched() {
        Runnable deleteLocal = mock(Runnable.class);
        Runnable unassign = mock(Runnable.class);
        doThrow(new IllegalStateException("MSBrand unavailable")).when(unassign).run();
        Deque<CompletedStep> completed = new ArrayDeque<>();
        completed.push(new CompletedStep(1, SagaStep.of("CREATE", () -> {}, "DELETE", deleteLocal)));
        completed.push(new CompletedStep(2, SagaStep.of("ASSIGN", () -> {}, "UNASSIGN", unassign)));
        assertFalse(compensator.compensate("saga", completed));
        verify(unassign, times(3)).run();
        verifyNoInteractions(deleteLocal);
        assertEquals(1, completed.size());
        verify(tracker).updateStatus("saga", SagaStatus.COMPENSATING);
        verify(tracker).log(eq("saga"), eq(2), eq("COMPENSATION_HALTED"), eq("COMPENSATE"), eq("FAILED"), anyString());
    }
}
