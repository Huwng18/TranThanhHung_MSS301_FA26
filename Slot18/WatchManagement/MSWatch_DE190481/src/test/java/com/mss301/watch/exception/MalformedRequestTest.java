package com.mss301.watch.exception;

import com.mss301.watch.controller.WatchController;
import com.mss301.watch.service.WatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDate;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// [TODO-6.15] Exercise real MVC binding failures before any Saga can start.
class MalformedRequestTest {
    private WatchService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = mock(WatchService.class);
        mvc = MockMvcBuilders.standaloneSetup(new WatchController(service))
            .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mvc.perform(post("/api/watches").header("X-User-Role", "1")
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false));
        verifyNoInteractions(service);
    }

    @Test
    void invalidDateReturns400() throws Exception {
        mvc.perform(post("/api/watches").header("X-User-Role", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validBody().replace(LocalDate.now().toString(), "not-a-date")))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false));
        verifyNoInteractions(service);
    }

    @Test
    void invalidPathIdentifierReturns400() throws Exception {
        mvc.perform(put("/api/watches/not-a-number").header("X-User-Role", "1")
                .contentType(MediaType.APPLICATION_JSON).content(validBody()))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false));
        verifyNoInteractions(service);
    }

    @Test
    void invalidFaultInjectionHeaderReturns400() throws Exception {
        mvc.perform(post("/api/watches").header("X-User-Role", "1")
                .header("X-Saga-Fail-After-Step", "not-a-number")
                .contentType(MediaType.APPLICATION_JSON).content(validBody()))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false));
        verifyNoInteractions(service);
    }

    @Test
    void missingRequestBodyReturns400() throws Exception {
        mvc.perform(post("/api/watches").header("X-User-Role", "1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false));
        verifyNoInteractions(service);
    }

    private static String validBody() {
        return """
            {"name":"Valid watch name","categoryId":1,"brandId":1,
             "caseMaterial":"Steel","strapMaterial":"Leather","waterpMaterial":"50 meters",
             "price":125.00,"releaseDate":"%s","stock":10}
            """.formatted(LocalDate.now());
    }
}
