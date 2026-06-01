package com.aperture.auth.repository;

import com.aperture.auth.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByEmailIgnoreCase(String email);

    Optional<AppUser> findByApiToken(String apiToken);

    boolean existsByEmailIgnoreCase(String email);
}
