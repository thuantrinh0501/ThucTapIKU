package com.example.thuc_tap_iku.service;

import com.example.thuc_tap_iku.dto.TaskDTO;
import com.example.thuc_tap_iku.entity.Task;
import com.example.thuc_tap_iku.entity.User;
import com.example.thuc_tap_iku.exception.AccessDeniedException;
import com.example.thuc_tap_iku.exception.ResourceNotFoundException;
import com.example.thuc_tap_iku.repository.TaskRepository;
import com.example.thuc_tap_iku.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TaskDTO> getTasksByCurrentUser() {
        User user = getCurrentUser();
        return taskRepository.findByUserId(user.getId()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TaskDTO createTask(TaskDTO dto, Long userId) {
        User targetUser = userId != null
                ? userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại (id: " + userId + ")"))
                : getCurrentUser();

        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setCompleted(dto.isCompleted());
        task.setUser(targetUser);

        Task saved = taskRepository.save(task);
        return convertToDTO(saved);
    }

    public TaskDTO getTaskById(Long id) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task không tồn tại (id: " + id + ")"));

        if (!"ADMIN".equals(currentUser.getRole()) && !task.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền xem task này!");
        }

        return convertToDTO(task);
    }

    public TaskDTO updateTask(Long id, TaskDTO dto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task không tồn tại (id: " + id + ")"));

        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setCompleted(dto.isCompleted());

        Task saved = taskRepository.save(task);
        return convertToDTO(saved);
    }

    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task không tồn tại (id: " + id + ")");
        }
        taskRepository.deleteById(id);
    }

    private TaskDTO convertToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setCompleted(task.isCompleted());
        dto.setUserId(task.getUser().getId());
        return dto;
    }

    public Page<TaskDTO> getTasksPaged(
            Integer page,
            Integer size,
            String status,       // completed | pending | null
            String sortBy,       // createdAt | deadline | title | ...
            String order         // asc | desc
    ) {
        User user = getCurrentUser();

        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 10;

        Sort.Direction direction =
                "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;

        Sort sort = Sort.by(direction, sortBy != null ? sortBy : "createdAt");

        Pageable pageable = PageRequest.of(pageNum, pageSize, sort);

        Page<Task> pageResult;

        // FILTER
        if ("completed".equalsIgnoreCase(status)) {
            pageResult = taskRepository.findByUserIdAndCompleted(user.getId(), true, pageable);
        } else if ("pending".equalsIgnoreCase(status)) {
            pageResult = taskRepository.findByUserIdAndCompleted(user.getId(), false, pageable);
        } else {
            pageResult = taskRepository.findByUserId(user.getId(), pageable);
        }

        return pageResult.map(this::convertToDTO);
    }

}
