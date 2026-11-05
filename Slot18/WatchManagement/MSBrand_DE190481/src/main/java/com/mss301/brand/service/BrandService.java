package com.mss301.brand.service;

import com.mss301.brand.dto.AssignmentResponse;
import com.mss301.brand.dto.BrandResponse;
import com.mss301.brand.dto.BrandWatchesResponse;
import com.mss301.brand.entity.BrandWatch;
import com.mss301.brand.entity.WatchBrand;
import com.mss301.brand.repository.BrandRepository;
import com.mss301.brand.repository.BrandWatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrandService {
    private final BrandRepository brandRepository;
    private final BrandWatchRepository brandWatchRepository;

    // [TODO-5.2] One local transaction; repeated assignment updates the same row.
    @Transactional
    public AssignmentResponse assignWatch(Integer brandId, Integer watchId) {
        if (!brandRepository.existsById(brandId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Brand not found with id: " + brandId);
        }
        BrandWatch link = brandWatchRepository.findByWatchId(watchId)
            .orElseGet(() -> BrandWatch.builder().watchId(watchId).build());
        Integer previous = link.getBrandId();
        link.setBrandId(brandId);
        link.setUpdatedAt(LocalDateTime.now());
        brandWatchRepository.save(link);
        log.info("[SAGA-PARTICIPANT] ASSIGN watch={} -> brand={} (previous brand={})",
            watchId, brandId, previous);
        return new AssignmentResponse(watchId, brandId, previous, "ASSIGNED");
    }

    // [TODO-5.3] Retrying an already completed compensation is a successful no-op.
    @Transactional
    public AssignmentResponse unassignWatch(Integer watchId) {
        var existing = brandWatchRepository.findByWatchId(watchId);
        if (existing.isEmpty()) {
            log.info("[SAGA-PARTICIPANT] UNASSIGN watch={} -> nothing to do", watchId);
            return new AssignmentResponse(watchId, null, null, "NOTHING_TO_DO");
        }
        Integer previous = existing.get().getBrandId();
        brandWatchRepository.delete(existing.get());
        log.info("[SAGA-PARTICIPANT] UNASSIGN watch={} from brand={}", watchId, previous);
        return new AssignmentResponse(watchId, null, previous, "UNASSIGNED");
    }

    // [TODO-5.5] Stable ordering makes seed data and Saga verification easy to inspect.
    @Transactional(readOnly = true)
    public List<BrandResponse> findAll() {
        return brandRepository.findAll(Sort.by("brandId")).stream()
            .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BrandResponse findById(Integer id) {
        return toResponse(getBrand(id));
    }

    @Transactional(readOnly = true)
    public BrandWatchesResponse watchesOf(Integer brandId) {
        WatchBrand brand = getBrand(brandId);
        List<Integer> watchIds = brandWatchRepository.findByBrandIdOrderByWatchIdAsc(brandId)
            .stream().map(BrandWatch::getWatchId).toList();
        return new BrandWatchesResponse(brandId, brand.getBrandName(), watchIds.size(), watchIds);
    }

    @Transactional(readOnly = true)
    public boolean existsById(Integer id) {
        return brandRepository.existsById(id);
    }

    private WatchBrand getBrand(Integer id) {
        return brandRepository.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found with id: " + id));
    }

    private BrandResponse toResponse(WatchBrand brand) {
        return new BrandResponse(brand.getBrandId(), brand.getBrandName(),
            brand.getCountryOfOrigin(), brandWatchRepository.countByBrandId(brand.getBrandId()));
    }
}
