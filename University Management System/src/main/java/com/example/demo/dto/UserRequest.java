package com.example.demo.dto;

import com.example.demo.model.Role;

public record UserRequest(
        String username,
        String password,
        String fullName,
        String email,
        String department,
        String studentId,
        Role role
) {
}
