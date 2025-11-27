package com.example.thuc_tap_iku.service;

import com.example.thuc_tap_iku.dto.UserDTO;
import com.example.thuc_tap_iku.entity.User;
import com.example.thuc_tap_iku.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException; // ĐÚNG IMPORT

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserDTO createUser(UserDTO dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username đã tồn tại");
        }
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã tồn tại");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(encoder.encode(dto.getPassword()));

        String role = dto.getRole() != null && !dto.getRole().isBlank()
                ? dto.getRole().toUpperCase()
                : "USER";
        if (!List.of("USER", "ADMIN").contains(role)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role phải là USER hoặc ADMIN");
        }
        user.setRole(role);

        User saved = userRepository.save(user);
        return convertToDTO(saved);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User không tồn tại (id: " + id + ")"));
        return convertToDTO(user);
    }

    public UserDTO updateUser(Long id, UserDTO dto) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User không tồn tại (id: " + id + ")"));

        if (!existing.getUsername().equals(dto.getUsername()) &&
                userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username đã tồn tại");
        }
        if (!existing.getEmail().equals(dto.getEmail()) &&
                userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã tồn tại");
        }

        existing.setUsername(dto.getUsername());
        existing.setEmail(dto.getEmail());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            existing.setPassword(encoder.encode(dto.getPassword()));
        }

        if (dto.getRole() != null && !dto.getRole().isBlank()) {
            String role = dto.getRole().toUpperCase();
            if (!List.of("USER", "ADMIN").contains(role)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role phải là USER hoặc ADMIN");
            }
            existing.setRole(role);
        }

        User saved = userRepository.save(existing);
        return convertToDTO(saved);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User không tồn tại (id: " + id + ")");
        }
        userRepository.deleteById(id);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        return dto;
    }
}
