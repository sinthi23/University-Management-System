package com.example.demo.config;

import com.example.demo.model.AppUser;
import com.example.demo.model.Role;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    private final UserRepository userRepository;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    CommandLineRunner seedUsers(PasswordEncoder encoder) {
        return args -> {
            createIfMissing("admin", "System Administrator", "admin@university.edu", null, null, Role.ADMIN, encoder);
            createIfMissing("teacher", "Dr. Sarah Wilson", "teacher@university.edu", "Computer Science", null, Role.TEACHER, encoder);
            createIfMissing("student", "Alex Johnson", "student@university.edu", "Computer Science", "CSE-2301", Role.STUDENT, encoder);
        };
    }

    private void createIfMissing(String username, String name, String email, String department,
                                 String studentId, Role role, PasswordEncoder encoder) {
        if (userRepository.existsByUsername(username)) return;
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(encoder.encode("password"));
        user.setFullName(name);
        user.setEmail(email);
        user.setDepartment(department);
        user.setStudentId(studentId);
        user.setRole(role);
        userRepository.save(user);
    }
}
