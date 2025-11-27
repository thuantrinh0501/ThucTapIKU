package com.example.thuc_tap_iku.repository;

import com.example.thuc_tap_iku.entity.Task;
import com.example.thuc_tap_iku.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // Custom: Tìm task theo user
    List<Task> findByUserId(Long userId);
    Optional<Task> findByIdAndUserId(Long id, Long userId);
    // Tìm task hoàn thành
    List<Task> findByCompleted(boolean completed);
    List<Task> findByUser(User user);
    Page<Task> findByUserId(Long userId, Pageable pageable);

    Page<Task> findByUserIdAndCompleted(Long userId, Boolean completed, Pageable pageable);
}