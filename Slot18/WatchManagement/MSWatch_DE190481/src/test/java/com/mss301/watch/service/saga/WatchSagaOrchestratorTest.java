package com.mss301.watch.service.saga;

import com.mss301.watch.client.BrandServiceClient;
import com.mss301.watch.entity.SagaState;
import com.mss301.watch.service.WatchData;
import com.mss301.watch.service.WatchLocalService;
import feign.FeignException;
import feign.Request;
import feign.Response;
import feign.RetryableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.json.JsonMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// [TODO-6.11] Real executor and compensator prove the seven required Saga scenarios.
class WatchSagaOrchestratorTest {
    private WatchLocalService local;
    private BrandServiceClient brand;
    private SagaTracker tracker;
    private WatchSagaOrchestrator orchestrator;
    private final WatchData before = data(11, 1, "Original watch name");
    private final WatchData after = data(11, 2, "Updated watch name");

    @BeforeEach
    void setUp() {
        local = mock(WatchLocalService.class);
        brand = mock(BrandServiceClient.class);
        tracker = mock(SagaTracker.class);
        when(tracker.start(anyString(), nullable(Integer.class), anyString()))
            .thenReturn(SagaState.builder().sagaId("saga").build());
        when(local.insert(any())).thenReturn(11);
        JsonMapper mapper = JsonMapper.builder().build();
        SagaFailureClassifier classifier = new SagaFailureClassifier(mapper);
        SagaCompensator compensator = new SagaCompensator(tracker, classifier);
        ReflectionTestUtils.setField(compensator, "backoffMs", 0L);
        SagaFaultInjector injector = new SagaFaultInjector();
        ReflectionTestUtils.setField(injector, "enabled", true);
        SagaExecutor executor = new SagaExecutor(tracker, injector, compensator, classifier);
        orchestrator = new WatchSagaOrchestrator(local, brand, tracker, executor, mapper);
    }

    @Test
    void scenarioACreateCommitsBothParticipants() {
        var result = orchestrator.createWatch(before, null);
        assertEquals(SagaStatus.COMPLETED, result.status());
        var order = inOrder(local, brand);
        order.verify(local).insert(before);
        order.verify(brand).assignWatch(1, 11);
        verify(local, never()).deleteIfExists(any());
    }

    @Test
    void scenarioBMissingBrandDeletesThePreviouslyCommittedWatch() {
        doThrow(missingBrand()).when(brand).assignWatch(1, 11);
        var error = assertThrows(SagaExecutionException.class,
            () -> orchestrator.createWatch(before, null));
        assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        assertEquals(SagaStatus.COMPENSATED, error.getSagaStatus());
        var order = inOrder(local, brand);
        order.verify(local).insert(before);
        order.verify(brand).assignWatch(1, 11);
        order.verify(local).deleteIfExists(11);
        verify(brand, never()).unassignWatch(any());
    }

    @Test
    void createFaultAfterTheFirstStepRetainsTheGeneratedIdForMonitoring() {
        var error = assertThrows(SagaExecutionException.class,
            () -> orchestrator.createWatch(before, 1));
        assertEquals(SagaStatus.COMPENSATED, error.getSagaStatus());
        verify(tracker).attachWatch("saga", 11);
        verify(local).deleteIfExists(11);
        verifyNoInteractions(brand);
    }

    @Test
    void scenarioCCreateFaultAfterRemoteCommitUnassignsBeforeDeleting() {
        var error = assertThrows(SagaExecutionException.class,
            () -> orchestrator.createWatch(before, 2));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getHttpStatus());
        assertEquals(SagaStatus.COMPENSATED, error.getSagaStatus());
        var order = inOrder(local, brand);
        order.verify(local).insert(before);
        order.verify(brand).assignWatch(1, 11);
        order.verify(brand).unassignWatch(11);
        order.verify(local).deleteIfExists(11);
    }

    @Test
    void scenarioDUpdateWithMissingBrandRestoresTheFullSnapshot() {
        doThrow(missingBrand()).when(brand).assignWatch(2, 11);
        var error = assertThrows(SagaExecutionException.class,
            () -> orchestrator.updateWatch(before, after, null));
        assertEquals(SagaStatus.COMPENSATED, error.getSagaStatus());
        var order = inOrder(local, brand);
        order.verify(local).update(after);
        order.verify(brand).assignWatch(2, 11);
        order.verify(local).update(before);
        verify(brand, never()).assignWatch(1, 11);
    }

    @Test
    void scenarioEUpdateFaultRestoresBrandBeforeTheLocalSnapshot() {
        assertThrows(SagaExecutionException.class,
            () -> orchestrator.updateWatch(before, after, 2));
        var order = inOrder(local, brand);
        order.verify(local).update(after);
        order.verify(brand).assignWatch(2, 11);
        order.verify(brand).assignWatch(1, 11);
        order.verify(local).update(before);
    }

    @Test
    void successfulUpdateDoesNotRunAnyCompensation() {
        assertEquals(SagaStatus.COMPLETED, orchestrator.updateWatch(before, after, null).status());
        verify(local).update(after);
        verify(local, never()).update(before);
        verify(brand).assignWatch(2, 11);
        verify(brand, never()).assignWatch(1, 11);
    }

    @Test
    void scenarioFDeleteFaultBeforePivotReassignsWithoutDeleting() {
        var error = assertThrows(SagaExecutionException.class,
            () -> orchestrator.deleteWatch(before, 1));
        assertEquals(SagaStatus.COMPENSATED, error.getSagaStatus());
        var order = inOrder(brand);
        order.verify(brand).unassignWatch(11);
        order.verify(brand).assignWatch(1, 11);
        verify(local, never()).delete(any());
    }

    @Test
    void scenarioGDeleteFaultAfterPivotIsSkipped() {
        assertEquals(SagaStatus.COMPLETED, orchestrator.deleteWatch(before, 2).status());
        var order = inOrder(brand, local);
        order.verify(brand).unassignWatch(11);
        order.verify(local).delete(11);
        verify(brand, never()).assignWatch(any(), any());
        verify(tracker).log(eq("saga"), eq(2), eq("FAULT_INJECTION"), eq("FAULT"), eq("SKIPPED"), anyString());
    }

    @Test
    void unreachableBrandDuringCreateReturns503AndDeletesLocalWatch() {
        doThrow(unreachable()).when(brand).assignWatch(1, 11);
        var error = assertThrows(SagaExecutionException.class,
            () -> orchestrator.createWatch(before, null));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getHttpStatus());
        assertTrue(error.getReason().contains("MSBrand is unreachable"));
        verify(local).deleteIfExists(11);
    }

    @Test
    void unreachableBrandDuringDeleteLeavesLocalWatchIntact() {
        doThrow(unreachable()).when(brand).unassignWatch(11);
        var error = assertThrows(SagaExecutionException.class,
            () -> orchestrator.deleteWatch(before, null));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getHttpStatus());
        verify(local, never()).delete(any());
        verify(brand, never()).assignWatch(any(), any());
    }

    @Test
    void compensationFailureRetriesThreeTimesAndStopsBeforeDeletingLocalWatch() {
        doThrow(unreachable()).when(brand).unassignWatch(11);
        var error = assertThrows(SagaExecutionException.class,
            () -> orchestrator.createWatch(before, 2));
        assertEquals(SagaStatus.COMPENSATING, error.getSagaStatus());
        verify(brand, times(3)).unassignWatch(11);
        verify(local, never()).deleteIfExists(any());
        verify(tracker).log(eq("saga"), eq(2), eq("COMPENSATION_HALTED"), eq("COMPENSATE"), eq("FAILED"), anyString());
    }

    private static WatchData data(Integer id, Integer brandId, String name) {
        return new WatchData(id, name, 1, brandId, "Steel", "Leather", "50 meters",
            new BigDecimal("125.00"), LocalDate.now(), 10);
    }

    private static Request request() {
        return Request.create(Request.HttpMethod.PUT, "http://ms-brand/internal/brands/999/watches/11",
            Map.of(), null, StandardCharsets.UTF_8, null);
    }

    private static FeignException missingBrand() {
        return FeignException.errorStatus("assignWatch", Response.builder().status(404).reason("Not Found")
            .request(request()).headers(Map.of())
            .body("{\"message\":\"Brand not found with id: 999\"}", StandardCharsets.UTF_8).build());
    }

    private static RetryableException unreachable() {
        return new RetryableException(-1, "Connection refused", Request.HttpMethod.PUT, (Long) null, request());
    }
}
