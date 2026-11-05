package com.mss301.account.dto;

// [TODO-3.4] Consistent envelope for successful and failed API responses.
public record ApiResponse<T>(boolean success, String message, T data) {
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data);
    }
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }
}
