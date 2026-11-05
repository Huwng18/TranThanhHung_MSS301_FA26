package com.mss301.watch.service;

import com.mss301.watch.dto.WatchRequest;
import com.mss301.watch.repository.WatchCategoryRepository;
import com.mss301.watch.repository.WatchRepository;
import com.mss301.watch.service.saga.WatchSagaOrchestrator;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// [TODO-6.13] Invalid input is rejected before starting a distributed transaction.
class WatchValidationTest {
    @Test
    void badDateNeverStartsASaga() {
        WatchSagaOrchestrator saga = mock(WatchSagaOrchestrator.class);
        WatchCategoryRepository categories = mock(WatchCategoryRepository.class);
        WatchService service = new WatchService(mock(WatchRepository.class), categories,
            mock(WatchLocalService.class), saga);
        WatchRequest request = validRequest();
        request.setReleaseDate(LocalDate.now().minusDays(1));
        var error = assertThrows(ResponseStatusException.class, () -> service.create(request, null));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verifyNoInteractions(saga, categories);
    }

    @Test
    void missingCategoryNeverStartsASaga() {
        WatchSagaOrchestrator saga = mock(WatchSagaOrchestrator.class);
        WatchCategoryRepository categories = mock(WatchCategoryRepository.class);
        WatchService service = new WatchService(mock(WatchRepository.class), categories,
            mock(WatchLocalService.class), saga);
        var error = assertThrows(ResponseStatusException.class, () -> service.create(validRequest(), null));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verifyNoInteractions(saga);
    }

    @Test
    void beanValidationEnforcesRequiredFieldsAndTheNamePriceBoundaries() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertFalse(validator.validate(new WatchRequest()).isEmpty());
            WatchRequest request = validRequest();
            request.setName("1234567890");
            request.setPrice(new BigDecimal("99.99"));
            var violations = validator.validate(request);
            assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
            assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("price")));
            request.setName("12345678901");
            request.setPrice(new BigDecimal("100.00"));
            assertTrue(validator.validate(request).isEmpty());
        }
    }

    private static WatchRequest validRequest() {
        WatchRequest request = new WatchRequest();
        request.setName("Valid watch name");
        request.setCategoryId(1);
        request.setBrandId(1);
        request.setCaseMaterial("Steel");
        request.setStrapMaterial("Leather");
        request.setWaterpMaterial("50 meters");
        request.setPrice(new BigDecimal("100.00"));
        request.setReleaseDate(LocalDate.now());
        request.setStock(1);
        return request;
    }
}
