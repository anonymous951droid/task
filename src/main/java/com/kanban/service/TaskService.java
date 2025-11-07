package com.kanban.service;

import com.kanban.dto.TaskRequestDto;
import com.kanban.dto.TaskResponseDto;
import com.kanban.entity.Task;
import com.kanban.exception.ResourceNotFoundException;
import com.kanban.mapper.TaskMapper;
import com.kanban.model.TaskStatus;
import com.kanban.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Transactional(readOnly = true)
    public Page<TaskResponseDto> getAllTasks(TaskStatus status, Pageable pageable) {
        Page<Task> tasks;
        if (status != null) {
            tasks = taskRepository.findByStatus(status, pageable);
        } else {
            tasks = taskRepository.findAll(pageable);
        }
        return tasks.map(taskMapper::toDto);
    }

    @Transactional(readOnly = true)
    public TaskResponseDto getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return taskMapper.toDto(task);
    }

    @Transactional
    public TaskResponseDto createTask(TaskRequestDto taskRequestDto) {
        Task task = taskMapper.toEntity(taskRequestDto);
        Task savedTask = taskRepository.save(task);
        log.info("Created task with id: {}", savedTask.getId());
        return taskMapper.toDto(savedTask);
    }

    @Transactional
    public TaskResponseDto updateTask(Long id, TaskRequestDto taskRequestDto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        
        taskMapper.updateEntityFromDto(taskRequestDto, task);
        Task updatedTask = taskRepository.save(task);
        log.info("Updated task with id: {}", updatedTask.getId());
        return taskMapper.toDto(updatedTask);
    }

    @Transactional
    public TaskResponseDto partialUpdateTask(Long id, TaskRequestDto taskRequestDto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        
        taskMapper.partialUpdateEntityFromDto(taskRequestDto, task);
        Task updatedTask = taskRepository.save(task);
        log.info("Partially updated task with id: {}", updatedTask.getId());
        return taskMapper.toDto(updatedTask);
    }

    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
        log.info("Deleted task with id: {}", id);
    }
}

