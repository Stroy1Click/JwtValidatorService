package ru.story1click.jwt.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.story1click.jwt.service.JwtService;

@RestController
@RequestMapping("/api/v1/jwt")
@RequiredArgsConstructor
@Tag(name = "JwtValidator Controller", description = "Проверка JWT для Ingress")
public class JwtValidatorController {

    private final JwtService jwtService;

    @GetMapping("/validate")
    public ResponseEntity<Void> validateJwt(@RequestHeader(value = "Authorization", required = false) String jwt,
                                            @RequestHeader(value = "X-Original-URL", required = false) String rawUri,
                                            @RequestHeader(value = "X-Original-Method", required = false) String rawMethod) {

        String originalUri = getCleanHeader(rawUri);
        String originalMethod = getCleanHeader(rawMethod);

        if (areHeadersMissing(originalMethod, originalUri)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if(isPreflightRequest(originalMethod)){
            return ResponseEntity.noContent().build();
        }

        if (isPublicAccessAllowed(originalMethod, originalUri)) {
            return ResponseEntity.ok().build();
        }

        if (!isBearerTokenPresent(jwt)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = jwt.substring(0, 7);

        if (!this.jwtService.validate(token, originalUri)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok().build();
    }

    private boolean areHeadersMissing(String method, String uri) {
        return method == null || uri == null;
    }

    private boolean isBearerTokenPresent(String jwt) {
        return jwt != null && jwt.startsWith("Bearer ");
    }

    private boolean isPublicAccessAllowed(String method, String uri) {
        return isWhitelistedEndpoint(uri) || isAllowedGetRequest(method, uri);
    }

    private boolean isPreflightRequest(String method) {
        return "OPTIONS".equalsIgnoreCase(method);
    }

    private boolean isWhitelistedEndpoint(String uri) {
        return uri.equals("/api/v1/auth/login")
                || uri.equals("/api/v1/auth/registration")
                || uri.equals("/api/v1/product-attribute-assignments/filter")
                || uri.startsWith("/api/v1/confirmation-codes");
    }

    private boolean isAllowedGetRequest(String method, String uri) {
        return "GET".equalsIgnoreCase(method)
                && !uri.startsWith("/api/v1/users")
                && !uri.startsWith("/api/v1/orders");
    }

    private String getCleanHeader(String header) {
        if (header == null) return null;
        if (header.contains(",")) {
            String[] parts = header.split(",");
            return parts[parts.length - 1].trim(); // Берем последний кусок после запятой
        }
        return header.trim();
    }
}
