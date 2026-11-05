package com.mss301.brand.controller;

import com.mss301.brand.dto.ApiResponse;
import com.mss301.brand.dto.AssignmentResponse;
import com.mss301.brand.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

// [TODO-5.4] Gateway never routes /internal/**; MSWatch calls this port directly.
@RestController
@RequestMapping("/internal/brands")
@RequiredArgsConstructor
@Tag(name = "Internal (Saga)")
public class InternalBrandController {
    private final BrandService brandService;

    @PutMapping("/{brandId}/watches/{watchId}")
    @Operation(summary = "Assign a watch to a brand (idempotent)")
    public ApiResponse<AssignmentResponse> assign(@PathVariable Integer brandId,
                                                 @PathVariable Integer watchId) {
        return ApiResponse.success(brandService.assignWatch(brandId, watchId), "Assigned");
    }

    @DeleteMapping("/watches/{watchId}")
    @Operation(summary = "Unassign a watch (idempotent)")
    public ApiResponse<AssignmentResponse> unassign(@PathVariable Integer watchId) {
        return ApiResponse.success(brandService.unassignWatch(watchId), "Unassigned");
    }
}
