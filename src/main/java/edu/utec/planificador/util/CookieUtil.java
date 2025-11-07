package edu.utec.planificador.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class CookieUtil {

    @Value("${security.cookie.secure:false}")
    private boolean secureCookie;

    @Value("${security.cookie.domain:}")
    private String cookieDomain;

    private final EncryptionUtil encryptionUtil;

    public CookieUtil(EncryptionUtil encryptionUtil) {
        this.encryptionUtil = encryptionUtil;
    }

    public static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    private static final String SAME_SITE_STRICT = "Strict";
    private static final String COOKIE_PATH = "/";

    public void addJwtCookie(HttpServletResponse response, String token, int maxAgeSeconds) {
        String encryptedToken = encryptionUtil.encrypt(token);

        Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, encryptedToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookie);
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge(maxAgeSeconds);

        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            cookie.setDomain(cookieDomain);
        }

        response.addCookie(cookie);

        addSameSiteAttribute(response, SAME_SITE_STRICT);

        log.debug("Encrypted JWT cookie added with maxAge: {} seconds, secure: {}", maxAgeSeconds, secureCookie);
    }

    public void deleteJwtCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookie);
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge(0);

        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            cookie.setDomain(cookieDomain);
        }

        response.addCookie(cookie);

        addSameSiteAttribute(response, SAME_SITE_STRICT);

        log.debug("JWT cookie deleted");
    }

    public Optional<String> getCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    try {
                        String decryptedValue = encryptionUtil.decrypt(cookie.getValue());
                        return Optional.of(decryptedValue);
                    } catch (Exception e) {
                        log.error("Error decrypting cookie value for: {}", cookieName, e);
                        return Optional.empty();
                    }
                }
            }
        }
        return Optional.empty();
    }

    private void addSameSiteAttribute(HttpServletResponse response, String sameSiteValue) {
        String setCookieHeader = response.getHeader("Set-Cookie");
        if (setCookieHeader != null && !setCookieHeader.contains("SameSite=")) {
            response.setHeader("Set-Cookie", setCookieHeader + "; SameSite=" + sameSiteValue);
        }
    }
}

