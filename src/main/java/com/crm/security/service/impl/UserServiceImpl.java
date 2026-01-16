package com.crm.security.service.impl;

import com.crm.security.dto.UserRequestDTO;
import com.crm.security.dto.UserResponseDTO;
import com.crm.security.exceptions.DuplicateUserException;
import com.crm.security.exceptions.UserNotFoundException;
import com.crm.security.mapper.UserMapper;
import com.crm.security.model.User;
import com.crm.security.repository.UserRepository;
import com.crm.security.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    public UserResponseDTO createUser(UserRequestDTO requestDTO) {
        log.info("Creating new user with username: {}", requestDTO.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(requestDTO.getUsername())) {
            throw new DuplicateUserException("Username already exists: " + requestDTO.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateUserException("Email already exists: " + requestDTO.getEmail());
        }

        User user = userMapper.toEntity(requestDTO, passwordEncoder);
        User savedUser = userRepository.save(user);

        log.info("User created successfully with id: {}", savedUser.getId());
        return userMapper.toDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        log.info("Fetching user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return userMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByUsername(String username) {
        log.info("Fetching user with username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("username", username));

        return userMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        log.info("Fetching all users");

        return userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO updateUser(Long id, UserRequestDTO requestDTO) {
        log.info("Updating user with id: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // Check if new username conflicts with another user
        if (!existingUser.getUsername().equals(requestDTO.getUsername()) &&
                userRepository.existsByUsername(requestDTO.getUsername())) {
            throw new DuplicateUserException("Username already exists: " + requestDTO.getUsername());
        }

        // Check if new email conflicts with another user
        if (!existingUser.getEmail().equals(requestDTO.getEmail()) &&
                userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateUserException("Email already exists: " + requestDTO.getEmail());
        }

        userMapper.updateEntity(existingUser, requestDTO, passwordEncoder);
        User updatedUser = userRepository.save(existingUser);

        log.info("User updated successfully with id: {}", updatedUser.getId());
        return userMapper.toDTO(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);

        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully with id: {}", id);
    }

    @Override
    public void enableUser(Long id) {
        log.info("Enabling user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setEnabled(true);
        userRepository.save(user);

        log.info("User enabled successfully with id: {}", id);
    }

    @Override
    public void disableUser(Long id) {
        log.info("Disabling user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setEnabled(false);
        userRepository.save(user);

        log.info("User disabled successfully with id: {}", id);
    }

    @Override
    public void lockUser(Long id) {
        log.info("Locking user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setAccountNonLocked(false);
        userRepository.save(user);

        log.info("User locked successfully with id: {}", id);
    }

    @Override
    public void unlockUser(Long id) {
        log.info("Unlocking user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setAccountNonLocked(true);
        userRepository.save(user);

        log.info("User unlocked successfully with id: {}", id);
    }
}