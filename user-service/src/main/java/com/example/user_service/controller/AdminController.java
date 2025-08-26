package com.example.user_service.controller;

import com.example.user_service.client.PersonClient;
import com.example.user_service.entity.UserEntity;
import com.example.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
    public ResponseEntity<?> getAllUsers() {
        List<UserEntity> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    // ✅ Tek kullanıcı detayını getir
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found")));
    }

    // ✅ Rol güncelleme
    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id,
                                            @RequestParam("role") String role,
                                            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // normalize
        String normalized = role.toUpperCase();
        final String finalRole = normalized.equals("YETKILI ÇALIŞAN") ? "PERSON" : normalized;

        return userRepository.findById(id)
                .map(user -> {
                    user.setRole(finalRole);

                    // Sadece PERSON için person-service çağrısı yapılır
                    if (finalRole.equals("PERSON")) {
                        try {
                            String token = (authHeader != null) ? authHeader.replace("Bearer ", "") : null;

                            Long personId = personClient.createPersonFromUser(
                                    user.getName(),
                                    user.getSurname(),
                                    user.getEmail(),
                                    user.getPhone(),
                                    null,   // departmanId başta null
                                    token
                            );

                            if (personId == null) {
                                log.warn("⚠️ Person oluşturulamadı, user={} email={}", user.getId(), user.getEmail());
                                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                                        .body(Map.of("error", "PersonService kaydı oluşturulamadı"));
                            }

                            user.setPersonId(personId);

                        } catch (Exception e) {
                            log.error("❌ PersonService çağrısı başarısız: {}", e.getMessage());
                            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                                    .body(Map.of("error", "PersonService hatası", "details", e.getMessage()));
                        }
                    }

                    // USER ve ADMIN → sadece User tablosuna yazılır
                    userRepository.save(user);

                    // ✅ Null değerleri güvenli şekilde döndürmek için HashMap kullanalım
                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "Rol güncellendi");
                    response.put("role", finalRole);
                    if (user.getPersonId() != null) {
                        response.put("personId", user.getPersonId());
                    }

                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found")));
    }

    // ✅ Kullanıcı sil
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .<ResponseEntity<?>>map(user -> {
                    userRepository.deleteById(id);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found")));
    }

    // ✅ Kullanıcı oluştur
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> payload) {
        String email = (String) payload.get("email");
        String password = (String) payload.get("password");
        String name = (String) payload.getOrDefault("name", "-");
        String surname = (String) payload.getOrDefault("surname", "-");
        String phone = (String) payload.getOrDefault("phone", "-");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "email ve password zorunlu"));
        }

        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Email zaten kayıtlı"));
        }

        UserEntity newUser = UserEntity.builder()
                .username(email)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .name(name)
                .surname(surname)
                .phone(phone)
                .role(null) // rol başta boş olacak
                .build();

        userRepository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    // ✅ Kullanıcı güncelleme (frontend edit için gerekli)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return userRepository.findById(id)
                .map(user -> {
                    if (payload.containsKey("name")) user.setName((String) payload.get("name"));
                    if (payload.containsKey("surname")) user.setSurname((String) payload.get("surname"));
                    if (payload.containsKey("email")) user.setEmail((String) payload.get("email"));
                    if (payload.containsKey("phone")) user.setPhone((String) payload.get("phone"));
                    if (payload.containsKey("password") && payload.get("password") != null) {
                        String rawPassword = (String) payload.get("password");
                        if (!rawPassword.isBlank()) {
                            user.setPasswordHash(passwordEncoder.encode(rawPassword));
                        }
                    }
                    userRepository.save(user);

                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "Kullanıcı güncellendi");
                    response.put("user", user);

                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found")));
    }
}
