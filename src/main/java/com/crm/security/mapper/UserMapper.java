package com.crm.security.mapper;

import com.crm.security.dto.UserRequestDTO;
import com.crm.security.dto.UserResponseDTO;
import com.crm.security.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponseDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        // ✅ Передаємо ролі на фронтенд
        dto.setRoles(user.getRoles() != null ? new HashSet<>(user.getRoles()) : new HashSet<>());
        dto.setEnabled(user.getEnabled());
        dto.setAccountNonLocked(user.getAccountNonLocked());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    public User toEntity(UserRequestDTO dto, PasswordEncoder passwordEncoder) {
        if (dto == null) {
            return null;
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRoles(new HashSet<>());
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        return user;
    }

    public void updateEntity(User user, UserRequestDTO dto, PasswordEncoder passwordEncoder) {
        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            user.setUsername(dto.getUsername());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
    }
}