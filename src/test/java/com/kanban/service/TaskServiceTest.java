package com.kanban.service;

import com.kanban.dto.TaskRequestDto;
import com.kanban.dto.TaskResponseDto;
import com.kanban.entity.Task;
import com.kanban.exception.ResourceNotFoundException;
import com.kanban.mapper.TaskMapper;
import com.kanban.model.TaskPriority;
import com.kanban.model.TaskStatus;
import com.kanban.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private TaskRequestDto taskRequestDto;
    private TaskResponseDto taskResponseDto;

    @BeforeEach
    void setUp() {
        task = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TO_DO)
                .priority(TaskPriority.MED)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        taskRequestDto = TaskRequestDto.builder()
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TO_DO)
                .priority(TaskPriority.MED)
                .build();

        taskResponseDto = TaskResponseDto.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TO_DO)
                .priority(TaskPriority.MED)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllTasks_WithoutStatus_ReturnsAllTasks() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        List<Task> tasks = Arrays.asList(task);
        Page<Task> taskPage = new PageImpl<>(tasks, pageable, 1);

        when(taskRepository.findAll(pageable)).thenReturn(taskPage);
        when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

        // When
        Page<TaskResponseDto> result = taskService.getAllTasks(null, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(taskRepository).findAll(pageable);
        verify(taskMapper).toDto(task);
    }

    @Test
    void getAllTasks_WithStatus_ReturnsFilteredTasks() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        List<Task> tasks = Arrays.asList(task);
        Page<Task> taskPage = new PageImpl<>(tasks, pageable, 1);

        when(taskRepository.findByStatus(TaskStatus.TO_DO, pageable)).thenReturn(taskPage);
        when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

        // When
        Page<TaskResponseDto> result = taskService.getAllTasks(TaskStatus.TO_DO, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(taskRepository).findByStatus(TaskStatus.TO_DO, pageable);
        verify(taskMapper).toDto(task);
    }

    @Test
    void getTaskById_ExistingId_ReturnsTask() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

        // When
        TaskResponseDto result = taskService.getTaskById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(taskRepository).findById(1L);
        verify(taskMapper).toDto(task);
    }

    @Test
    void getTaskById_NonExistingId_ThrowsException() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(1L));
        verify(taskRepository).findById(1L);
        verify(taskMapper, never()).toDto(any());
    }

    @Test
    void createTask_ValidRequest_ReturnsCreatedTask() {
        // Given
        when(taskMapper.toEntity(taskRequestDto)).thenReturn(task);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

        // When
        TaskResponseDto result = taskService.createTask(taskRequestDto);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(taskMapper).toEntity(taskRequestDto);
        verify(taskRepository).save(any(Task.class));
        verify(taskMapper).toDto(task);
    }

    @Test
    void updateTask_ExistingId_ReturnsUpdatedTask() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

        // When
        TaskResponseDto result = taskService.updateTask(1L, taskRequestDto);

        // Then
        assertNotNull(result);
        verify(taskRepository).findById(1L);
        verify(taskMapper).updateEntityFromDto(taskRequestDto, task);
        verify(taskRepository).save(task);
        verify(taskMapper).toDto(task);
    }

    @Test
    void updateTask_NonExistingId_ThrowsException() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> taskService.updateTask(1L, taskRequestDto));
        verify(taskRepository).findById(1L);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void partialUpdateTask_ExistingId_ReturnsUpdatedTask() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

        // When
        TaskResponseDto result = taskService.partialUpdateTask(1L, taskRequestDto);

        // Then
        assertNotNull(result);
        verify(taskRepository).findById(1L);
        verify(taskMapper).partialUpdateEntityFromDto(taskRequestDto, task);
        verify(taskRepository).save(task);
        verify(taskMapper).toDto(task);
    }

    @Test
    void deleteTask_ExistingId_DeletesTask() {
        // Given
        when(taskRepository.existsById(1L)).thenReturn(true);

        // When
        taskService.deleteTask(1L);

        // Then
        verify(taskRepository).existsById(1L);
        verify(taskRepository).deleteById(1L);
    }

    @Test
    void deleteTask_NonExistingId_ThrowsException() {
        // Given
        when(taskRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> taskService.deleteTask(1L));
        verify(taskRepository).existsById(1L);
        verify(taskRepository, never()).deleteById(any());
    }
}

