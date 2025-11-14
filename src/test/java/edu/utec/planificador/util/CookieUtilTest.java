package edu.utec.planificador.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CookieUtil Unit Tests")
class CookieUtilTest {

    @Mock
    private EncryptionUtil encryptionUtil;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpServletRequest request;

    private CookieUtil cookieUtil;

    private static final String TEST_TOKEN = "test.jwt.token";
    private static final String ENCRYPTED_TOKEN = "encrypted.token";

    @BeforeEach
    void setUp() {
        cookieUtil = new CookieUtil(encryptionUtil);
        ReflectionTestUtils.setField(cookieUtil, "secureCookie", false);
        ReflectionTestUtils.setField(cookieUtil, "cookieDomain", "");
    }

    @Test
    @DisplayName("Should add JWT cookie with encryption")
    void addJwtCookie_Success() {
        int maxAge = 3600;
        when(encryptionUtil.encrypt(TEST_TOKEN)).thenReturn(ENCRYPTED_TOKEN);

        cookieUtil.addJwtCookie(response, TEST_TOKEN, maxAge);

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());
        verify(encryptionUtil).encrypt(TEST_TOKEN);

        Cookie capturedCookie = cookieCaptor.getValue();
        assertThat(capturedCookie.getName()).isEqualTo("access_token");
        assertThat(capturedCookie.getValue()).isEqualTo(ENCRYPTED_TOKEN);
        assertThat(capturedCookie.isHttpOnly()).isTrue();
        assertThat(capturedCookie.getMaxAge()).isEqualTo(maxAge);
    }

    @Test
    @DisplayName("Should get and decrypt cookie value")
    void getCookieValue_Success() {
        Cookie cookie = new Cookie("access_token", ENCRYPTED_TOKEN);
        Cookie[] cookies = {cookie};

        when(request.getCookies()).thenReturn(cookies);
        when(encryptionUtil.decrypt(ENCRYPTED_TOKEN)).thenReturn(TEST_TOKEN);

        Optional<String> result = cookieUtil.getCookieValue(request, "access_token");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(TEST_TOKEN);
        verify(encryptionUtil).decrypt(ENCRYPTED_TOKEN);
    }

    @Test
    @DisplayName("Should return empty when cookie not found")
    void getCookieValue_NotFound() {
        Cookie cookie = new Cookie("other_cookie", "value");
        Cookie[] cookies = {cookie};

        when(request.getCookies()).thenReturn(cookies);

        Optional<String> result = cookieUtil.getCookieValue(request, "access_token");

        assertThat(result).isEmpty();
        verify(encryptionUtil, never()).decrypt(anyString());
    }
}

