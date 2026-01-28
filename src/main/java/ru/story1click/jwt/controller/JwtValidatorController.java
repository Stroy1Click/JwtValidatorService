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
                                            @RequestHeader(value = "X-Original-Method", required = false) String rawMethod){
        String originalUri = getCleanHeader(rawUri);
        String originalMethod = getCleanHeader(rawMethod);

        System.out.println("Jwt" + jwt);
        System.out.println("X-Original-URL" + originalUri);
        System.out.println("X-Original-Method" + originalMethod);

        if (originalMethod == null || originalUri == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if(originalMethod.equalsIgnoreCase("OPTIONS")){ //CORS
            return ResponseEntity.noContent().build();
        }

        if (originalUri.equals("/api/v1/auth/login") || originalUri.equals("/api/v1/auth/registration")
                || originalUri.equals("/api/v1/product-attribute-assignments/filter") ||
                originalUri.startsWith("/api/v1/confirmation-codes")) {
            return ResponseEntity.ok().build();
        }

        if (originalMethod.equalsIgnoreCase("GET")
                && !originalUri.startsWith("/api/v1/users")
                && !originalUri.startsWith("/api/v1/orders")) {
            return ResponseEntity.ok().build();
        }

        if (jwt == null || !jwt.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if(!this.jwtService.validate(jwt.substring(7), originalUri)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok().build();
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
