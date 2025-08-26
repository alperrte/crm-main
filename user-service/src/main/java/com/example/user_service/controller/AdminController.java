package com.example.user_service.controller;

import com.example.user_service.client.PersonClient;
import com.example.user_service.entity.UserEntity;
import com.example.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // ✅ sadece ADMIN erişebilir
@Slf4j
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PersonClient personClient;

    // ✅ TÜM kullanıcıları listele
    @GetMapping
    public ResponseEntity<List<UserEntity>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // ✅ Tek kullanıcı detayını getir
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found")));
    }

    // ✅ Person’dan User oluştur (Kullanıcı Yap)
    @PostMapping("/from-person/{personId}")
    public ResponseEntity<?> createUserFromPerson(@PathVariable Long personId,
                                                  @RequestBody Map<String, String> payload,
                                                  @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String token = (authHeader != null) ? authHeader.replace("Bearer ", "") : null;

            Map<String, Object> person = personClient.getPersonById(personId, token);

            if (person == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Person not found"));
            }

            String email = (String) person.get("email");
            if (email == null || userRepository.existsByEmail(email)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Email zaten kullanıcıya ait"));
            }

            String rawPassword = payload.get("password");
            if (rawPassword == null || rawPassword.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Password zorunlu"));
            }

            UserEntity user = UserEntity.builder()
                    .personId(person.get("id") != null ? Long.valueOf(person.get("id").toString()) : null)
                    .name((String) person.get("name"))
                    .surname((String) person.get("surname"))
                    .email(email)
                    .phone((String) person.get("phone"))
                    .passwordHash(passwordEncoder.encode(rawPassword))
                    .role("USER") // ✅ başlangıç rolü
                    .build();

            userRepository.save(user);

            return ResponseEntity.status(HttpStatus.CREATED).body(user);

        } catch (Exception e) {
            log.error("❌ Person’dan user oluşturulamadı: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "PersonService hatası", "details", e.getMessage()));
        }
    }

    // ✅ Rol güncelleme
    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestParam("role") String role) {
        return userRepository.findById(id)
                .map(user -> {
                    String normalized = role.toUpperCase();
                    if (!List.of("USER", "PERSON", "ADMIN").contains(normalized)) {
                        return ResponseEntity.badRequest().body(Map.of("error", "Geçersiz rol"));
                    }
                    user.setRole(normalized);
                    userRepository.save(user);
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("message", "Rol güncellendi");
                    resp.put("role", user.getRole());
                    return ResponseEntity.ok(resp);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found")));
    }

    // ✅ Kullanıcı sil
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(u -> {
                    userRepository.deleteById(id);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found")));
    }
}
