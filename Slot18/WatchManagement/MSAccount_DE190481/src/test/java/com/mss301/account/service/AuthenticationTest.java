package com.mss301.account.service;

import com.mss301.account.dto.LoginRequest;
import com.mss301.account.dto.LoginResponse;
import com.mss301.account.entity.StoreAccount;
import com.mss301.account.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// [TODO-3.5] Test real signatures and BCrypt rather than mocking token validity.
class AuthenticationTest {
    private static final String SECRET = "mss301-account-test-key-at-least-32-bytes-long";
    private final AccountRepository accounts = mock(AccountRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final JwtService jwt = new JwtService(SECRET, 900000, 604800000);
    private final AuthService auth = new AuthService(accounts, encoder, jwt);
    private StoreAccount admin;

    @BeforeEach
    void setUp() {
        admin = StoreAccount.builder().accountId(1).username("adminsys")
            .email("admin@drawingpen.com.vn").password(encoder.encode("@1")).role(1).build();
        when(accounts.findByUsername("adminsys")).thenReturn(Optional.of(admin));
        when(accounts.findById(1)).thenReturn(Optional.of(admin));
    }

    @Test
    void loginIssuesSeparateAccessAndRefreshClaims() {
        LoginResponse response = auth.login(new LoginRequest("adminsys", "@1"));
        var access = jwt.parseToken(response.accessToken());
        var refresh = jwt.parseToken(response.refreshToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals("1", access.getSubject());
        assertEquals(1, access.get("role", Integer.class));
        assertEquals("access", access.get("type"));
        assertEquals("refresh", refresh.get("type"));
        assertNull(refresh.get("role"));
        assertEquals(900000, access.getExpiration().getTime() - access.getIssuedAt().getTime());
        assertEquals(604800000, refresh.getExpiration().getTime() - refresh.getIssuedAt().getTime());
        assertTrue(admin.getIsActive());
        assertNotEquals("@1", admin.getPassword());
    }

    @Test
    void incorrectCredentialsReturnUnauthorized() {
        var error = assertThrows(ResponseStatusException.class,
            () -> auth.login(new LoginRequest("adminsys", "wrong")));
        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
        verify(accounts, never()).findById(anyInt());
    }

    @Test
    void inactiveAccountCannotLogin() {
        admin.setIsActive(false);
        var error = assertThrows(ResponseStatusException.class,
            () -> auth.login(new LoginRequest("adminsys", "@1")));
        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }

    @Test
    void refreshAcceptsRefreshTokenAndUsesCurrentRole() {
        String token = jwt.generateRefreshToken("1");
        admin.setRole(2);
        var response = auth.refresh(token);
        assertEquals(2, jwt.parseToken(response.accessToken()).get("role", Integer.class));
        assertEquals("refresh", jwt.parseToken(response.refreshToken()).get("type"));
    }

    @Test
    void refreshRejectsAnAccessToken() {
        var error = assertThrows(ResponseStatusException.class,
            () -> auth.refresh(jwt.generateAccessToken("1", 1)));
        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
        verify(accounts, never()).findById(anyInt());
    }

    @Test
    void refreshRejectsAccountDisabledAfterTokenWasIssued() {
        String token = jwt.generateRefreshToken("1");
        admin.setIsActive(false);
        var error = assertThrows(ResponseStatusException.class, () -> auth.refresh(token));
        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }

    @Test
    void expiredForgedAndMalformedTokensAreRejected() {
        JwtService expired = new JwtService(SECRET, -60000, -60000);
        JwtService forged = new JwtService("different-test-secret-with-at-least-32-bytes", 900000, 604800000);
        for (String token : new String[] {expired.generateRefreshToken("1"),
                forged.generateRefreshToken("1"), "malformed", jwt.generateRefreshToken("not-an-id")}) {
            var error = assertThrows(ResponseStatusException.class, () -> auth.refresh(token));
            assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
        }
        assertFalse(jwt.isTokenValid(null));
    }
}
