package com.example.user_service.controller;

import com.example.user_service.entity.UserEntity;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.client.PersonClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // ✅ sadece ADMIN erişebilir
@Slf4j
public class AdminController {

    private final UserRepository userRepository;
    private final PersonClient personClient;

    // ✅ TÜM kullanıcıları listele
    @GetMapping
    public ResponseEntity<List<UserEntity>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // ✅ Tek kullanıcı detayını getir
    @GetMapping("/{id}")
    public ResponseEntity<UserEntity> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Generic rol değiştirme (ADMIN / USER)
    @PutMapping("/{id}/role")
    public ResponseEntity<String> updateUserRole(@PathVariable Long id,
                                                 @RequestParam("role") String role) {
        if (role == null) {
            return ResponseEntity.badRequest().body("Role parametresi boş olamaz");
        }

        String normalized = role.toUpperCase();
        if (!List.of("ADMIN", "USER").contains(normalized)) {
            return ResponseEntity.badRequest().body("Geçersiz rol: " + role);
        }

        return userRepository.findById(id)
                .map(user -> {
                    user.setRole(normalized);
                    userRepository.save(user);
                    return ResponseEntity.ok("Rol güncellendi: " + normalized);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Kullanıcıyı PERSON yap ve person-service'e kaydet
    @PutMapping("/{id}/make-person")
    public ResponseEntity<?> makePerson(@PathVariable Long id,
                                        @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return userRepository.findById(id)
                .map(user -> {
                    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("error", "Authorization header eksik veya hatalı"));
                    }
                    String token = authHeader.substring(7);

                    try {
                        Long personId = personClient.createPersonFromUser(
                                user.getName() != null ? user.getName() : user.getUsername(),
                                user.getSurname() != null ? user.getSurname() : "-",
                                user.getEmail(),
                                user.getPhone(),
                                token
                        );

                        if (personId == null) {
                            log.warn("⚠️ personId null döndü, users.person_id set edilmedi!");
                            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                                    .body(Map.of("error", "Person kaydı açılamadı"));
                        }

                        user.setPersonId(personId);
                        user.setRole("PERSON");
                        userRepository.save(user);

                        return ResponseEntity.ok(Map.of(
                                "message", "Kullanıcı PERSON yapıldı",
                                "email", user.getEmail(),
                                "personId", personId
                        ));

                    } catch (Exception e) {
                        log.error("❌ makePerson sırasında hata", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("error", "makePerson hatası: " + e.getMessage()));
                    }
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found")));
    }

    // ✅ Kullanıcıyı sil
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
