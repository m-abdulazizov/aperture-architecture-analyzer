package com.aperture.auth.payload;

import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String email,
        String token
) {
}
