package com.mss301.watch.service.saga;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

// [TODO-6.15] Map participant failures to useful HTTP responses.
@Component
@RequiredArgsConstructor
public class SagaFailureClassifier {
    private final JsonMapper jsonMapper;
    public String describe(RuntimeException ex) {
        if (ex instanceof FeignException fe) {
            if (fe.status() == 404) {
                return "MSBrand returned 404: " + remoteMessage(fe, "brand not found");
            }
            if (fe.status() < 0) {                       // RetryableException: không kết nối được
                return "MSBrand is unreachable: " + fe.getMessage();
            }
            return "MSBrand returned HTTP " + fe.status() + ": " + remoteMessage(fe, fe.getMessage());
        }
        return ex.getClass().getSimpleName() + ": " + ex.getMessage();
    }

    /** Lấy trường "message" trong ApiResponse lỗi do MSBrand trả về. */
    private String remoteMessage(FeignException fe, String fallback) {
        try {
            String body = fe.contentUTF8();
            if (body != null && !body.isBlank()) {
                var node = jsonMapper.readTree(body).get("message");
                if (node != null && !node.isNull()) {
                    return node.asString();
                }
            }
        } catch (RuntimeException ignored) {
            // body không phải JSON -> dùng fallback
        }
        return fallback;
    }

    public HttpStatus httpStatusFor(RuntimeException ex) {
        if (ex instanceof FeignException fe) {
            if (fe.status() == 404) return HttpStatus.BAD_REQUEST;              // dữ liệu không hợp lệ (brand không tồn tại)
            if (fe.status() < 0 || fe.status() >= 500) return HttpStatus.SERVICE_UNAVAILABLE;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;                                 // gồm cả lỗi giả lập
    }

}
