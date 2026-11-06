package com.example.demo.service;

import com.example.demo.dto.UserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.AppUser;
import com.example.demo.model.Role;
import com.example.demo.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> findByRole(Role role) {
        return userRepository.findByRole(role).stream().map(UserResponse::from).toList();
    }

    public UserResponse find(Long id, Role role) {
        AppUser user = userRepository.findById(id)
                .filter(item -> item.getRole() == role)
                .orElseThrow(() -> new ResourceNotFoundException("No " + role.name().toLowerCase() + " found with id " + id));
        return UserResponse.from(user);
    }

    public UserResponse create(UserRequest request, Role role) {
        validate(request, role, null);
        AppUser user = new AppUser();
        apply(user, request, role, true);
        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse update(Long id, UserRequest request, Role role) {
        AppUser user = userRepository.findById(id)
                .filter(item -> item.getRole() == role)
                .orElseThrow(() -> new ResourceNotFoundException("No " + role.name().toLowerCase() + " found with id " + id));
        validate(request, role, user);
        apply(user, request, role, false);
        return UserResponse.from(userRepository.save(user));
    }

    public void delete(Long id, Role role) {
        AppUser user = userRepository.findById(id)
                .filter(item -> item.getRole() == role)
                .orElseThrow(() -> new ResourceNotFoundException("No " + role.name().toLowerCase() + " found with id " + id));
        userRepository.delete(user);
    }

    private void validate(UserRequest request, Role role, AppUser existing) {
        if (request.fullName() == null || request.fullName().isBlank() ||
                request.email() == null || request.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Full name and email are required");
        }
        if (role == Role.STUDENT && (request.studentId() == null || request.studentId().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student ID is required");
        }
        if (existing == null && (request.username() == null || request.username().isBlank() ||
                request.password() == null || request.password().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and password are required");
        }
        if (request.username() != null && !request.username().isBlank() &&
                userRepository.findByUsername(request.username()).filter(user -> existing == null || !user.getId().equals(existing.getId())).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already in use");
        }
        if (userRepository.findAll().stream().anyMatch(user -> user.getEmail().equalsIgnoreCase(request.email())
                && (existing == null || !user.getId().equals(existing.getId())))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use");
        }
    }

    private void apply(AppUser user, UserRequest request, Role role, boolean creating) {
        if (creating) user.setUsername(request.username().trim());
        if (request.password() != null && !request.password().isBlank()) user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setDepartment(role == Role.ADMIN ? null : request.department());
        user.setStudentId(role == Role.STUDENT ? request.studentId() : null);
        user.setRole(role);
    }
}
