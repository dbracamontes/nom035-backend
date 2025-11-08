package com.example.nom035.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.nom035.entity.PasswordResetToken;
import com.example.nom035.entity.User;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByExpiresAtBefore(LocalDateTime cutoff);
    List<PasswordResetToken> findByUserAndUsedFalse(User user);
}
