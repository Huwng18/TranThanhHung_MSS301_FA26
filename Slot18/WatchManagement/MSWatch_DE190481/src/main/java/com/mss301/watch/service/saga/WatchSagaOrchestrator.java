package com.mss301.watch.service.saga;

import com.mss301.watch.client.BrandServiceClient;
import com.mss301.watch.service.WatchData;
import com.mss301.watch.service.WatchLocalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

// [TODO-6.11] Assemble local transactions and their inverse operations.
@Service
@RequiredArgsConstructor
public class WatchSagaOrchestrator {
    private final WatchLocalService localService;
    private final BrandServiceClient brandClient;
    private final SagaTracker tracker;
    private final SagaExecutor executor;
    private final JsonMapper jsonMapper;

    public SagaOutcome createWatch(WatchData data, Integer failAfterStep) {
        String sagaId = tracker.start("CREATE_WATCH", null, toJson(Map.of("after", data))).getSagaId();
        AtomicReference<Integer> watchId = new AtomicReference<>();   // bước 1 sinh ra id, bước 2 dùng lại

        List<SagaStep> steps = List.of(
            SagaStep.of("CREATE_WATCH",
                () -> {
                    watchId.set(localService.insert(data));

                },
                "DELETE_WATCH",
                () -> localService.deleteIfExists(watchId.get()))
                .withAfterCommit(() -> tracker.attachWatch(sagaId, watchId.get())),

            SagaStep.of("ASSIGN_BRAND",
                () -> {
                    brandClient.assignWatch(data.brandId(), watchId.get());
                },
                "UNASSIGN_BRAND",
                () -> brandClient.unassignWatch(watchId.get()))
        );

        executor.execute(sagaId, steps, failAfterStep);
        return new SagaOutcome(sagaId, watchId.get(), SagaStatus.COMPLETED);
    }

    // =========================================================================================
    // 2) UPDATE: cập nhật watch (DBWatch) -> chuyển watch sang brand mới (DBBrand)
    // =========================================================================================
    // [TODO-6.11b: Saga UPDATE_WATCH]
    public SagaOutcome updateWatch(WatchData before, WatchData after, Integer failAfterStep) {
        Integer id = before.watchId();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("before", before);           // snapshot để RESTORE nếu thất bại
        payload.put("after", after);
        String sagaId = tracker.start("UPDATE_WATCH", id, toJson(payload)).getSagaId();

        List<SagaStep> steps = List.of(
            SagaStep.of("UPDATE_WATCH",
                () -> localService.update(after),
                "RESTORE_WATCH",
                () -> localService.update(before)),

            SagaStep.of("ASSIGN_BRAND",
                () -> brandClient.assignWatch(after.brandId(), id),
                "ASSIGN_BRAND_BACK",
                () -> brandClient.assignWatch(before.brandId(), id))
        );

        executor.execute(sagaId, steps, failAfterStep);
        return new SagaOutcome(sagaId, id, SagaStatus.COMPLETED);
    }

    // =========================================================================================
    // 3) DELETE: gỡ watch khỏi brand (DBBrand) -> xoá watch (DBWatch, PIVOT)
    //    Thứ tự này có chủ ý: bước "xoá cứng" khó hoàn tác (IDENTITY sinh id mới) nên đặt CUỐI làm pivot.
    // =========================================================================================
    // [TODO-6.11c: Saga DELETE_WATCH (pivot cuối)]
    public SagaOutcome deleteWatch(WatchData before, Integer failAfterStep) {
        Integer id = before.watchId();
        String sagaId = tracker.start("DELETE_WATCH", id, toJson(Map.of("before", before))).getSagaId();

        List<SagaStep> steps = List.of(
            SagaStep.of("UNASSIGN_BRAND",
                () -> brandClient.unassignWatch(id),
                "ASSIGN_BRAND_BACK",
                () -> brandClient.assignWatch(before.brandId(), id)),

            SagaStep.pivot("DELETE_WATCH",
                () -> localService.delete(id))
        );

        executor.execute(sagaId, steps, failAfterStep);
        return new SagaOutcome(sagaId, id, SagaStatus.COMPLETED);
    }

    // =========================================================================================
    // "Động cơ" Saga dùng chung cho cả 3 nghiệp vụ

    private String toJson(Object value) { return jsonMapper.writeValueAsString(value); }
}
