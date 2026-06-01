package com.aperture.auth.service;

import com.aperture.auth.entity.AppUser;
import com.aperture.auth.payload.AuthRequest;
import com.aperture.auth.payload.AuthResponse;
import com.aperture.auth.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;

    @Transactional
    public AuthResponse register(AuthRequest request) {
        if (appUserRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        AppUser user = appUserRepository.save(AppUser.builder()
                .email(request.email().toLowerCase())
                .passwordHash(hash(request.password()))
                .apiToken("apt_" + UUID.randomUUID())
                .build());

        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        AppUser user = appUserRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!user.getPasswordHash().equals(hash(request.password()))) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse me(String authorizationHeader) {
        String token = authorizationHeader == null ? "" : authorizationHeader.replace("Bearer ", "");
        return appUserRepository.findByApiToken(token)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
    }

    private AuthResponse toResponse(AppUser user) {
        return new AuthResponse(user.getId(), user.getEmail(), user.getApiToken());
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
