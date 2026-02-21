package net.suevern.vokabel.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import net.suevern.vokabel.entity.User;
import net.suevern.vokabel.repository.UserRepository;
import net.suevern.vokabel.security.CustomUserDetailsService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('POWERUSER')")
public class UserManagementApiController {

    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public UserManagementApiController(UserRepository userRepository,
                                       CustomUserDetailsService userDetailsService,
                                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String name = payload.get("name");
        String password = payload.get("password");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
        }
        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
        }

        if (userDetailsService.userExists(email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "User with this email already exists"));
        }

        try {
            String encodedPassword = passwordEncoder.encode(password);
            User user = userDetailsService.createUser(email, encodedPassword, name);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to create user: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<?> disableUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setEnabled(false);
                    user.setUpdatedAt(LocalDateTime.now());
                    userRepository.save(user);
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/enable")
    public ResponseEntity<?> enableUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setEnabled(true);
                    user.setUpdatedAt(LocalDateTime.now());
                    userRepository.save(user);
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String newPassword = payload.get("password");

        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
        }

        return userRepository.findById(id)
                .map(user -> {
                    user.setPassword(passwordEncoder.encode(newPassword));
                    user.setUpdatedAt(LocalDateTime.now());
                    userRepository.save(user);
                    return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}

