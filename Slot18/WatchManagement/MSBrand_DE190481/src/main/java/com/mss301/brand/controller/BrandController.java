package com.mss301.brand.controller;

import com.mss301.brand.dto.ApiResponse;
import com.mss301.brand.dto.BrandResponse;
import com.mss301.brand.dto.BrandWatchesResponse;
import com.mss301.brand.service.BrandService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// [TODO-5.5] Authentication is performed by the API Gateway.
@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
@Tag(name = "Brands")
public class BrandController {
    private final BrandService brandService;

    @GetMapping
    public ApiResponse<List<BrandResponse>> findAll() {
        return ApiResponse.success(brandService.findAll(), "Success");
    }

    @GetMapping("/{id}")
    public ApiResponse<BrandResponse> findById(@PathVariable Integer id) {
        return ApiResponse.success(brandService.findById(id), "Success");
    }

    @GetMapping("/{id}/watches")
    public ApiResponse<BrandWatchesResponse> watches(@PathVariable Integer id) {
        return ApiResponse.success(brandService.watchesOf(id), "Success");
    }

    @GetMapping("/{id}/exists")
    public boolean exists(@PathVariable Integer id) {
        return brandService.existsById(id);
    }
}
