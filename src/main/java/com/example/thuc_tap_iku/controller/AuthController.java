package com.example.thuc_tap_iku.controller;

import com.example.thuc_tap_iku.config.JwtUtil;
import com.example.thuc_tap_iku.dto.AuthResponse;
import com.example.thuc_tap_iku.dto.LoginDTO;
import com.example.thuc_tap_iku.dto.UserDTO;
import com.example.thuc_tap_iku.entity.User;
import com.example.thuc_tap_iku.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // Đăng ký
    @PostMapping("/dangky")
    public ResponseEntity<String> register(@RequestBody UserDTO dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username đã tồn tại");
        }
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email đã tồn tại");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        // CHO PHÉP CHỌN ROLE
        String role = dto.getRole() != null && !dto.getRole().isEmpty() ? dto.getRole().toUpperCase() : "USER";
        if (!List.of("USER", "ADMIN").contains(role)) {
            return ResponseEntity.badRequest().body("Role phải là USER hoặc ADMIN");
        }
        user.setRole(role);

        userRepository.save(user);
        return ResponseEntity.ok("Đăng ký thành công với role: " + role);
    }


    // Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginDTO dto) {
        // Kiểm tra username
        User user = userRepository.findByUsername(dto.getUsername())
                .orElse(null);

        if(user == null) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, "Username không tồn tại"));
        }

        // Kiểm tra password
        if(!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, "Password không đúng"));
        }

        // Tạo token
        String token = jwtUtil.generateToken(user.getUsername());

        // Trả token + message
        return ResponseEntity.ok(new AuthResponse(token, "Login thành công"));
    }




}