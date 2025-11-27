package com.example.thuc_tap_iku.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private boolean error = true;
    private String message;
    private List<String> details;
    private String path;
    private LocalDateTime timestamp = LocalDateTime.now();
}
