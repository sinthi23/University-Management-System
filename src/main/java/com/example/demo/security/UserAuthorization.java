package com.example.demo.security;

import com.example.demo.model.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("userAuthorization")
public class UserAuthorization {
    public boolean canRead(String resource) { return canAccess(resource, false); }
    public boolean canManage(String resource) { return canAccess(resource, true); }

    private boolean canAccess(String resource, boolean manage) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return false;
        String currentRole = authentication.getAuthorities().stream().findFirst()
                .map(authority -> authority.getAuthority().replace("ROLE_", "")).orElse("");
        if ("ADMIN".equals(currentRole)) return true;
        return (Role.TEACHER.name().equals(currentRole)
                && ("teachers".equalsIgnoreCase(resource) || "students".equalsIgnoreCase(resource)))
                || (Role.STUDENT.name().equals(currentRole) && "students".equalsIgnoreCase(resource));
    }
}
