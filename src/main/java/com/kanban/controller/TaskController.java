package com.kanban.controller;

import com.kanban.dto.TaskRequestDto;
import com.kanban.dto.TaskResponseDto;
import com.kanban.model.TaskStatus;
import com.kanban.service.TaskService;
import com.kanban.service.WebSocketNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management API")
public class TaskController {

    private final TaskService taskService;
    private final WebSocketNotificationService webSocketNotificationService;

    @GetMapping
    @Operation(summary = "List tasks", description = "Get paginated list of tasks with optional filtering by status")
    public ResponseEntity<Page<TaskResponseDto>> getAllTasks(
            @Parameter(description = "Filter by task status") @RequestParam(required = false) TaskStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<TaskResponseDto> tasks = taskService.getAllTasks(status, pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task details", description = "Get task by ID")
    public ResponseEntity<TaskResponseDto> getTaskById(
            @Parameter(description = "Task ID") @PathVariable Long id) {
        TaskResponseDto task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @PostMapping
    @Operation(summary = "Create task", description = "Create a new task")
    public ResponseEntity<TaskResponseDto> createTask(@Valid @RequestBody TaskRequestDto taskRequestDto) {
        TaskResponseDto createdTask = taskService.createTask(taskRequestDto);
        webSocketNotificationService.notifyTaskCreated(createdTask);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task", description = "Full update of a task (uses optimistic locking)")
    public ResponseEntity<TaskResponseDto> updateTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @Valid @RequestBody TaskRequestDto taskRequestDto) {
        TaskResponseDto updatedTask = taskService.updateTask(id, taskRequestDto);
        webSocketNotificationService.notifyTaskUpdated(updatedTask);
        return ResponseEntity.ok(updatedTask);
    }

    @PatchMapping(value = "/{id}", consumes = "application/merge-patch+json")
    @Operation(summary = "Partial update task", description = "Partial update of a task using JSON Merge Patch")
    public ResponseEntity<TaskResponseDto> partialUpdateTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @RequestBody TaskRequestDto taskRequestDto) {
        TaskResponseDto updatedTask = taskService.partialUpdateTask(id, taskRequestDto);
        webSocketNotificationService.notifyTaskUpdated(updatedTask);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task", description = "Delete a task by ID")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "Task ID") @PathVariable Long id) {
        taskService.deleteTask(id);
        webSocketNotificationService.notifyTaskDeleted(id);
        return ResponseEntity.noContent().build();
    }
}

