package com.example.user_service.controller;

import com.example.user_service.entity.UserEntity;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.client.PersonClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    // ✅ TÜM kullanıcıları listele
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        List<UserEntity> users = userRepository.findAll();
        log.info("📤 {} kullanıcı bulundu", users.size());
        return ResponseEntity.ok(users);
    }

    // ✅ Tek kullanıcı detayını getir
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .<ResponseEntity<?>>map(user -> {
                    log.info("📤 Kullanıcı bulundu: {}", user.getEmail());
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> {
                    log.warn("⚠️ Kullanıcı bulunamadı: id={}", id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "User not found"));
                });
    }

    // ✅ Generic rol değiştirme (ADMIN / USER)
    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id,
                                            @RequestParam("role") String role) {
        if (role == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Role parametresi boş olamaz"));
        }

        String normalized = role.toUpperCase();
        if (!List.of("ADMIN", "USER").contains(normalized)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Geçersiz rol: " + role));
        }

        return userRepository.findById(id)
                .<ResponseEntity<?>>map(user -> {
                    user.setRole(normalized);
                    userRepository.save(user);
                    log.info("🔄 Kullanıcı rolü güncellendi: {} -> {}", user.getEmail(), normalized);
                    return ResponseEntity.ok(Map.of("message", "Rol güncellendi", "role", normalized));
                })
                .orElseGet(() -> {
                    log.warn("⚠️ Rol güncelleme başarısız: id={} bulunamadı", id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "User not found"));
                });
    }

    // ✅ Kullanıcıyı PERSON yap ve person-service'e kaydet
    @PutMapping("/{id}/make-person")
    public ResponseEntity<?> makePerson(@PathVariable Long id,
                                        @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return userRepository.findById(id)
                .<ResponseEntity<?>>map(user -> {
                    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("error", "Authorization header eksik veya hatalı"));
                    }
                    String token = authHeader.substring(7);

                    try {
                        Long personId = personClient.createPersonFromUser(
                                user.getName() != null ? user.getName() : "-",
                                user.getSurname() != null ? user.getSurname() : "-",
                                user.getEmail(),
                                user.getPhone(),
                                token
                        );

                        if (personId == null) {
                            log.warn("⚠️ Person kaydı açılamadı: {}", user.getEmail());
                            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                                    .body(Map.of("error", "Person kaydı açılamadı"));
                        }

                        user.setPersonId(personId);
                        user.setRole("PERSON");
                        userRepository.save(user);

                        log.info("✅ User {} artık PERSON (personId={})", user.getEmail(), personId);

                        return ResponseEntity.ok(Map.of(
                                "message", "Kullanıcı PERSON yapıldı",
                                "email", user.getEmail(),
                                "personId", personId
                        ));

                    } catch (Exception e) {
                        log.error("❌ makePerson sırasında hata: {}", user.getEmail(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("error", "makePerson hatası: " + e.getMessage()));
                    }
                })
                .orElseGet(() -> {
                    log.warn("⚠️ makePerson başarısız: id={} bulunamadı", id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "User not found"));
                });
    }

    // ✅ Kullanıcıyı sil (Person da varsa beraber silinsin)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id,
                                        @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return userRepository.findById(id)
                .<ResponseEntity<?>>map(user -> {
                    if (user.getPersonId() != null) {
                        try {
                            String token = (authHeader != null && authHeader.startsWith("Bearer "))
                                    ? authHeader.substring(7)
                                    : null;

                            if (token != null) {
                                personClient.deletePerson(user.getPersonId(), token);
                                log.info("🗑 Person {} silindi (User ile birlikte)", user.getPersonId());
                            } else {
                                log.warn("⚠️ Person silinemedi çünkü Authorization token eksik");
                            }
                        } catch (Exception e) {
                            log.error("❌ Person silinemedi, id={}", user.getPersonId(), e);
                        }
                    }

                    userRepository.deleteById(id);
                    log.info("🗑 User {} silindi", id);

                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> {
                    log.warn("⚠️ Silinecek kullanıcı bulunamadı: id={}", id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "User not found"));
                });
    }

    // ✅ Yeni kullanıcı oluştur (manuel ekleme, default role=USER)
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> payload) {
        log.info("📥 Yeni kullanıcı payload: {}", payload);

        String email = payload.get("email");
        String password = payload.get("password");
        String name = payload.getOrDefault("name", "-");
        String surname = payload.getOrDefault("surname", "-");
        String phone = payload.getOrDefault("phone", "-");

        if (email == null || password == null) {
            log.error("❌ createUser: email veya password null geldi");
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "email ve password zorunlu"));
        }

        if (userRepository.existsByEmail(email)) {
            log.warn("⚠️ createUser: email zaten kayıtlı {}", email);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Email zaten kayıtlı"));
        }

        try {
            UserEntity newUser = UserEntity.builder()
                    .username(email)
                    .email(email)
                    .passwordHash(passwordEncoder.encode(password))
                    .name(name)
                    .surname(surname)
                    .phone(phone)
                    .role("USER")
                    .build();

            userRepository.save(newUser);
            log.info("✅ Yeni kullanıcı kaydedildi: {}", newUser.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);

        } catch (Exception e) {
            log.error("❌ createUser sırasında hata oluştu", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "createUser hatası: " + e.getMessage()));
        }
    }
}
