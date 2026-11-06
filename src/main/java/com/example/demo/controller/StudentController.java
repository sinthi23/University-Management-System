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
@RequestMapping("/api/students")
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
public class StudentController {
    private final UserService userService;

    public StudentController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> getStudents() {
        return userService.findByRole(Role.STUDENT);
    }

    @GetMapping("/{id}")
    public UserResponse getStudent(@PathVariable Long id) {
        return userService.find(id, Role.STUDENT);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createStudent(@RequestBody UserRequest request) {
        UserResponse created = userService.create(request, Role.STUDENT);
        return ResponseEntity.created(URI.create("/api/students/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public UserResponse updateStudent(@PathVariable Long id, @RequestBody UserRequest request) {
        return userService.update(id, request, Role.STUDENT);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        userService.delete(id, Role.STUDENT);
        return ResponseEntity.noContent().build();
    }
}
