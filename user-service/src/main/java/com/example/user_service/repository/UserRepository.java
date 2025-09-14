package com.example.user_service.repository;

import com.example.user_service.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // Kullanıcıyı email adresine göre arar.
    Optional<UserEntity> findByEmail(String email);
    // Verilen email adresine sahip bir kullanıcı veritabanında mevcut mu kontrol eder.
    boolean existsByEmail(String email);
}
