package com.mss301.account.service;

import com.mss301.account.dto.LoginRequest;
import com.mss301.account.dto.LoginResponse;
import com.mss301.account.entity.StoreAccount;
import com.mss301.account.repository.AccountRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

// [TODO-3.3] Authenticate BCrypt credentials and recheck account state on refresh.
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AccountRepository accounts;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        StoreAccount account = accounts.findByUsername(request.username())
            .orElseThrow(() -> unauthorized("Invalid username or password"));
        if (!passwordEncoder.matches(request.password(), account.getPassword())) {
            throw unauthorized("Invalid username or password");
        }
        ensureActive(account);
        return issueTokens(account);
    }

    public LoginResponse refresh(String refreshToken) {
        Claims claims;
        int accountId;
        try {
            claims = jwtService.parseToken(refreshToken);
            if (!"refresh".equals(claims.get("type", String.class))) {
                throw unauthorized("Token is not a refresh token");
            }
            accountId = Integer.parseInt(claims.getSubject());
        } catch (JwtException | IllegalArgumentException ex) {
            throw unauthorized("Invalid or expired refresh token");
        }
        StoreAccount account = accounts.findById(accountId)
            .orElseThrow(() -> unauthorized("Account not found"));
        ensureActive(account);
        return issueTokens(account);
    }

    private void ensureActive(StoreAccount account) {
        if (!Boolean.TRUE.equals(account.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is inactive");
        }
    }

    private LoginResponse issueTokens(StoreAccount account) {
        String subject = account.getAccountId().toString();
        return new LoginResponse(jwtService.generateAccessToken(subject, account.getRole()),
            jwtService.generateRefreshToken(subject), "Bearer", account.getRole(), account.getUsername());
    }

    private ResponseStatusException unauthorized(String message) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
    }
}
