package com.mss301.account.dto;

// [TODO-3.3] Token response deliberately excludes the stored password hash.
public record LoginResponse(String accessToken, String refreshToken, String tokenType,
                            Integer role, String username) {}
