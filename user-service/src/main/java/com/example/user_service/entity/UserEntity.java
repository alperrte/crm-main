package com.example.user_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private Long id; // PK

    @Column(name = "user_name")
    private String username; // Girişte kullanılacak benzersiz alan

    @Column(name = "password_hash")
    private String passwordHash; // BCrypt hash

    @Column(name = "role", nullable = false, length = 64)
    private String role; // ROLE_USER / ROLE_ADMIN

    @Column(name = "person_id")
    private Long personId; // İsteğe bağlı eşleştirme

    @Column(name = "refresh_token_hash")
    private String refreshTokenHash; // Refresh token'ın BCrypt hash'i

    @Column(name = "refresh_token_expires_at")
    private LocalDateTime refreshTokenExpires; // Refresh token son kullanma tarihi

}
