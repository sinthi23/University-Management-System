package com.example.demo.controller;

import com.example.demo.dto.UserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.model.Role;
import com.example.demo.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admins")
    public List<UserResponse> getAdmins() {
        return userService.findByRole(Role.ADMIN);
    }

    @GetMapping("/admins/{id}")
    public UserResponse getAdmin(@PathVariable Long id) {
        return userService.find(id, Role.ADMIN);
    }

    @PostMapping("/admins")
    public ResponseEntity<UserResponse> createAdmin(@RequestBody UserRequest request) {
        return created("admins", userService.create(request, Role.ADMIN));
    }

    @PutMapping("/admins/{id}")
    public UserResponse updateAdmin(@PathVariable Long id, @RequestBody UserRequest request) {
        return userService.update(id, request, Role.ADMIN);
    }

    @DeleteMapping("/admins/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long id) {
        userService.delete(id, Role.ADMIN);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<UserResponse> created(String resource, UserResponse response) {
        return ResponseEntity.created(URI.create("/api/" + resource + "/" + response.id())).body(response);
    }
}
