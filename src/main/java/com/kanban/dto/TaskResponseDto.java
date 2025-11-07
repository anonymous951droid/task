package com.kanban.dto;

import com.kanban.model.TaskPriority;
import com.kanban.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponseDto {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

