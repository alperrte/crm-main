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
    private Long id;

    @Column(name = "user_name")
    private String username;

    @Column(name = "email")
    private String email; // yeni

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "role")
    private String role; // 'ADMIN' | 'USER' (DB tarafı böyle, Spring tarafında ROLE_ prefix mapping yapacağız)

    @Column(name = "person_id")
    private Long personId; // opsiyonel, şu an register’dan almıyoruz

    @Column(name = "refresh_token_hash")
    private String refreshTokenHash; // refresh token plain (rotate için)

    @Column(name = "refresh_token_expires_at")
    private LocalDateTime refreshTokenExpires;
}