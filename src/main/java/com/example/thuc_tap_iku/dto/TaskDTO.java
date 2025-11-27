package com.example.thuc_tap_iku.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaskDTO {
    private Long id;

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(min = 3, max = 100, message = "Tiêu đề phải từ 3-100 ký tự")
    private String title;

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String description;

    private boolean completed;

    private Long userId;

//    // Optional: hiển thị tên user
//    private String username;
}
