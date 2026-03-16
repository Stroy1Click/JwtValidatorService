package ru.story1click.jwt.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.story1click.jwt.service.JwtValidationService;
import ru.stroy1click.common.service.JwtService;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtValidationServiceImpl implements JwtValidationService {

    private final JwtService jwtService;

    @Override
    public boolean validate(String jwt, String originalUri) {
        try {
            String role = this.jwtService.extractRole(jwt);

            boolean hasRole;

            if (originalUri.startsWith("/api/v1/users") || originalUri.startsWith("/api/v1/orders")) {
                hasRole = role.equals("ROLE_USER") || role.equals("ROLE_ADMIN");
            } else {
                hasRole = role.equals("ROLE_ADMIN");
            }

            Claims claims = this.jwtService.extractAllClaims(jwt);
            boolean notExpired = claims.getExpiration().after(new Date());
            boolean isEmailConfirmed = (boolean) claims.get("emailConfirmed");

            return hasRole && notExpired && isEmailConfirmed;
        } catch (JwtException e) {
            return false;
        }
    }
}
