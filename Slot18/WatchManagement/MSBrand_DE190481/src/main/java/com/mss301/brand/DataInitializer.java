package com.mss301.brand;

import com.mss301.brand.entity.BrandWatch;
import com.mss301.brand.entity.WatchBrand;
import com.mss301.brand.repository.BrandRepository;
import com.mss301.brand.repository.BrandWatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

// [TODO-5.6] Initial databases must be created together so WatchID values match.
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final BrandRepository brandRepository;
    private final BrandWatchRepository brandWatchRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // Seed links only on first initialization. An empty link table may mean
        // the administrator deleted all watches, and must stay empty on restart.
        if (brandRepository.count() != 0) {
            return;
        }
        List<WatchBrand> brands = List.of(
            WatchBrand.builder().brandName("Rolex").countryOfOrigin("Switzerland").build(),
            WatchBrand.builder().brandName("Omega").countryOfOrigin("Switzerland").build(),
            WatchBrand.builder().brandName("Seiko").countryOfOrigin("Japan").build(),
            WatchBrand.builder().brandName("Casio").countryOfOrigin("Japan").build(),
            WatchBrand.builder().brandName("TAG Heuer").countryOfOrigin("Switzerland").build()
        );
        brandRepository.saveAll(brands);
        brandRepository.flush();
        log.info("Seeded {} brands", brands.size());
        int[] watchToBrand = {1, 2, 3, 4, 5, 2, 3, 1, 4, 4};
        for (int i = 0; i < watchToBrand.length; i++) {
            brandWatchRepository.save(BrandWatch.builder().watchId(i + 1)
                .brandId(watchToBrand[i]).updatedAt(LocalDateTime.now()).build());
        }
        log.info("Seeded {} brand-watch links", watchToBrand.length);
    }
}
