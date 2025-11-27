package com.example.thuc_tap_iku.controller;

import com.example.thuc_tap_iku.dto.ApiResponse;
import com.example.thuc_tap_iku.dto.TaskDTO;
import com.example.thuc_tap_iku.entity.User;
import com.example.thuc_tap_iku.exception.AccessDeniedException;
import com.example.thuc_tap_iku.service.TaskService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private TaskService taskService;

    /**
     * Get current user - works for both production (User entity) and test (@WithMockUser)
     */
    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        // Production: principal is User entity
        if (principal instanceof User) {
            return (User) principal;
        }

        // Test: principal is UserDetails from @WithMockUser
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            org.springframework.security.core.userdetails.UserDetails userDetails =
                    (org.springframework.security.core.userdetails.UserDetails) principal;

            // Create mock User for test
            User user = new User();
            user.setUsername(userDetails.getUsername());

            // Extract role from authorities (ROLE_ADMIN -> ADMIN)
            String role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .orElse("USER");
            user.setRole(role);
            user.setId(1L); // Mock ID for test

            return user;
        }

        throw new IllegalStateException("Unknown principal type: " + principal.getClass());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskDTO>>> getTasks() {  // ← Đổi return type
        User currentUser = getCurrentUser();

        log.info(">>> [DEBUG] Username: {}", currentUser.getUsername());
        log.info(">>> [DEBUG] User ID: {}", currentUser.getId());
        log.info(">>> [DEBUG] Role: '{}'", currentUser.getRole());

        List<TaskDTO> tasks;
        if ("ADMIN".equals(currentUser.getRole())) {
            log.info(">>> [ADMIN MODE] Return all tasks");
            tasks = taskService.getAllTasks();
        } else {
            log.info(">>> [USER MODE] Return tasks for userId = {}", currentUser.getId());
            tasks = taskService.getTasksByCurrentUser();
        }

        // ✅ Wrap trong ApiResponse
        return ResponseEntity.ok(ApiResponse.success("Tasks retrieved successfully!", tasks));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskDTO>> createTask(
            @Valid @RequestBody TaskDTO dto,
            @RequestParam(required = false) Long userId) {

        boolean isAdmin = SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new AccessDeniedException("Only ADMIN can create task!");
        }

        User currentUser = getCurrentUser();
        log.info("ADMIN {} creating task for userId: {}", currentUser.getUsername(), userId);
        TaskDTO created = taskService.createTask(dto, userId);
        return ResponseEntity.ok(ApiResponse.success("Task created successfully!", created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskDTO>> getTask(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        TaskDTO task = taskService.getTaskById(id);

        // USER can only view their own tasks
        if (!"ADMIN".equals(currentUser.getRole()) && !task.getUserId().equals(currentUser.getId())) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("You don't have permission to view this task!", null));
        }

        return ResponseEntity.ok(ApiResponse.success("Task retrieved successfully!", task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskDTO>> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskDTO dto) {

        User currentUser = getCurrentUser();

        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Only ADMIN can update task!");
        }

        log.info("ADMIN {} updating task ID: {}", currentUser.getUsername(), id);
        TaskDTO updated = taskService.updateTask(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully!", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTask(@PathVariable Long id) {
        User currentUser = getCurrentUser();

        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Only ADMIN can delete task!");
        }

        log.info("ADMIN {} deleting task ID: {}", currentUser.getUsername(), id);
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully!", null));
    }

    private Map<String, Object> errorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", true);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    private Map<String, Object> successResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    @GetMapping("/paged")
    public Page<TaskDTO> getTasksPaged(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String order
    ) {
        return taskService.getTasksPaged(page, size, status, sortBy, order);
    }
}
