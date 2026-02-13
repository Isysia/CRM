package com.crm.security.service.impl;

import com.crm.security.dto.ChangeRoleRequest;
import com.crm.security.dto.RegisterRequest;
import com.crm.security.dto.UserRequestDTO;
import com.crm.security.dto.UserResponseDTO;
import com.crm.security.exceptions.DuplicateUserException;
import com.crm.security.exceptions.UserNotFoundException;
import com.crm.security.mapper.UserMapper;
import com.crm.security.model.Role;
import com.crm.security.model.User;
import com.crm.security.repository.UserRepository;
import com.crm.security.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void registerNewUser(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUserException("Nazwa użytkownika jest już zajęta: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("Email jest już zajęty: " + request.getEmail());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Set<String> roles = new HashSet<>();
        roles.add(Role.USER.name());
        user.setRoles(roles);

        user.setEnabled(true);
        user.setAccountNonLocked(true);

        userRepository.save(user);
        log.info("User registered successfully: {}", user.getUsername());
    }

    @Override
    public void changeUserRole(Long id, ChangeRoleRequest request) {
        log.info("Changing role for user id: {} to {}", id, request.getRole());

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        try {
            Role newRole = Role.valueOf(request.getRole());

            if (newRole == Role.ADMIN) {
                log.warn("Attempt to change role to ADMIN for user {} - blocked", id);
                throw new IllegalArgumentException("Nie można zmienić roli na ADMIN przez interfejs");
            }

            Set<String> roles = new HashSet<>();
            roles.add(newRole.name());
            user.setRoles(roles);

            userRepository.save(user);
            log.info("Role changed successfully for user {} to {}", id, newRole);
        } catch (IllegalArgumentException e) {
            log.error("Invalid role: {}", request.getRole());
            throw new IllegalArgumentException("Nieprawidłowa rola: " + request.getRole());
        }
    }

    @Override
    public UserResponseDTO createUser(UserRequestDTO requestDTO) {
        log.info("Creating new user with username: {}", requestDTO.getUsername());
        if (userRepository.existsByUsername(requestDTO.getUsername())) {
            throw new DuplicateUserException("Username already exists: " + requestDTO.getUsername());
        }
        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateUserException("Email already exists: " + requestDTO.getEmail());
        }
        User user = userMapper.toEntity(requestDTO, passwordEncoder);
        User savedUser = userRepository.save(user);
        return userMapper.toDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("username", username));
        return userMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO updateUser(Long id, UserRequestDTO requestDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!existingUser.getUsername().equals(requestDTO.getUsername()) &&
                userRepository.existsByUsername(requestDTO.getUsername())) {
            throw new DuplicateUserException("Username already exists");
        }
        if (!existingUser.getEmail().equals(requestDTO.getEmail()) &&
                userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateUserException("Email already exists");
        }

        userMapper.updateEntity(existingUser, requestDTO, passwordEncoder);
        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDTO(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) throw new UserNotFoundException(id);
        userRepository.deleteById(id);
    }

    @Override
    public void enableUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public void disableUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    public void lockUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.setAccountNonLocked(false);
        userRepository.save(user);
    }

    @Override
    public void unlockUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.setAccountNonLocked(true);
        userRepository.save(user);
    }
}