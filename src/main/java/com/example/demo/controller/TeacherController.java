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
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
public class TeacherController {
    private final UserService userService;

    public TeacherController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/teachers")
    public List<UserResponse> getTeachers() {
        return userService.findByRole(Role.TEACHER);
    }

    @GetMapping("/teachers/{id}")
    public UserResponse getTeacher(@PathVariable Long id) {
        return userService.find(id, Role.TEACHER);
    }

    @PostMapping("/teachers")
    public ResponseEntity<UserResponse> createTeacher(@RequestBody UserRequest request) {
        return created("teachers", userService.create(request, Role.TEACHER));
    }

    @PutMapping("/teachers/{id}")
    public UserResponse updateTeacher(@PathVariable Long id, @RequestBody UserRequest request) {
        return userService.update(id, request, Role.TEACHER);
    }

    @DeleteMapping("/teachers/{id}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        userService.delete(id, Role.TEACHER);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<UserResponse> created(String resource, UserResponse response) {
        return ResponseEntity.created(URI.create("/api/" + resource + "/" + response.id())).body(response);
    }
}
