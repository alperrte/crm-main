package com.example.user_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Kayıt (register) isteği için DTO.
 * - username: zorunlu, benzersiz
 * - password: min 6 karakter
 * - role    : boş gelirse service tarafında "ROLE_USER" atanır
 * - personId: opsiyonel (kişiyi önceden oluşturduysan eşlemek için)
 */
public record RegisterRequest(
        @NotBlank(message="Şifre alanı boş olamaz!") String username,
        @Size(min = 6, max=72, message="Şifre minumum 6 karakter olabilir.") @NotBlank String password,
        String role,
        Long personId
) {}