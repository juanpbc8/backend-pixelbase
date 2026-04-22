package com.pixelbase.backend.modules.admin.controller;

import com.pixelbase.backend.catalog.repository.CategoryRepository;
import com.pixelbase.backend.catalog.repository.ProductRepository;
import com.pixelbase.backend.common.exception.ResourceNotFoundException;
import com.pixelbase.backend.modules.user.domain.Role;
import com.pixelbase.backend.modules.user.domain.UserEntity;
import com.pixelbase.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @GetMapping("/stats")
    public Map<String, Long> stats() {
        return Map.of(
                "users", userRepository.count(),
                "products", productRepository.count(),
                "categories", categoryRepository.count()
        );
    }

    @GetMapping("/users")
    public List<UserEntity> listUsers() {
        return userRepository.findAll();
    }

    @PutMapping("/users/{id}/promote")
    public ResponseEntity<UserEntity> promoteToAdmin(@PathVariable Integer id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
        user.setRole(Role.ADMIN);
        return ResponseEntity.ok(userRepository.save(user));
    }
}
