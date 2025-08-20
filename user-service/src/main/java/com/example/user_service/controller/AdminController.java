package com.example.user_service.controller;

import com.example.user_service.entity.UserEntity;
import com.example.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final UserRepository userRepository;

    @GetMapping
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok("Kullanıcı silindi: " + id);
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<String> updateUserRole(@PathVariable Long id, @RequestParam String role) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setRole(role);
                    userRepository.save(user);
                    return ResponseEntity.ok("Rol güncellendi: " + role);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Kullanıcıyı PERSON yap
    @PutMapping("/{id}/make-person")
    public ResponseEntity<String> makePerson(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setRole("PERSON"); // rolü PERSON’a çevir
                    userRepository.save(user);
                    return ResponseEntity.ok("Kullanıcı PERSON yapıldı: " + user.getUsername());
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
