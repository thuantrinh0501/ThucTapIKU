package com.example.thuc_tap_iku.controller;

import com.example.thuc_tap_iku.dto.ApiResponse;
import com.example.thuc_tap_iku.dto.UserDTO;
import com.example.thuc_tap_iku.entity.User;
import com.example.thuc_tap_iku.exception.AccessDeniedException;
import com.example.thuc_tap_iku.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    private void requireAdmin(String action) {
        User currentUser = getCurrentUser();
        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Chỉ ADMIN mới được " + action + " user!");
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@RequestBody UserDTO dto) {
        requireAdmin("tạo");
        UserDTO created = userService.createUser(dto);
        return ResponseEntity.ok(ApiResponse.success("Tạo user thành công!", created));
    }

    // SỬA: DÙNG ApiResponse
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        requireAdmin("xem danh sách");
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách user thành công!", users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        requireAdmin("xem chi tiết");
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin user thành công!", user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable Long id, @RequestBody UserDTO dto) {
        requireAdmin("sửa");
        UserDTO updated = userService.updateUser(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật user thành công!", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        requireAdmin("xóa");
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa user thành công!", null));
    }
}
