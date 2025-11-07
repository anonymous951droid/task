package com.kanban.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanban.dto.TaskRequestDto;
import com.kanban.entity.Task;
import com.kanban.model.TaskPriority;
import com.kanban.model.TaskStatus;
import com.kanban.repository.TaskRepository;
import com.kanban.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Transactional
class TaskControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("kanban_test_db")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    @BeforeEach
    void setUp() {
        jwtToken = jwtTokenProvider.generateToken("testuser");
        taskRepository.deleteAll();
    }

    @Test
    void createTask_ValidRequest_ReturnsCreated() throws Exception {
        TaskRequestDto request = TaskRequestDto.builder()
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TO_DO)
                .priority(TaskPriority.MED)
                .build();

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.status").value("TO_DO"));
    }

    @Test
    void createTask_InvalidRequest_ReturnsBadRequest() throws Exception {
        TaskRequestDto request = TaskRequestDto.builder()
                .title("") // Invalid: empty title
                .build();

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTaskById_ExistingId_ReturnsTask() throws Exception {
        Task task = Task.builder()
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TO_DO)
                .priority(TaskPriority.MED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Task savedTask = taskRepository.save(task);

        mockMvc.perform(get("/api/tasks/{id}", savedTask.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTask.getId()))
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    void getTaskById_NonExistingId_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/tasks/999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllTasks_WithPagination_ReturnsPaginatedResults() throws Exception {
        // Create multiple tasks
        for (int i = 0; i < 5; i++) {
            Task task = Task.builder()
                    .title("Task " + i)
                    .status(TaskStatus.TO_DO)
                    .priority(TaskPriority.MED)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            taskRepository.save(task);
        }

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(5));
    }

    @Test
    void getAllTasks_WithStatusFilter_ReturnsFilteredResults() throws Exception {
        Task task1 = Task.builder()
                .title("Task 1")
                .status(TaskStatus.TO_DO)
                .priority(TaskPriority.MED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        taskRepository.save(task1);

        Task task2 = Task.builder()
                .title("Task 2")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.MED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        taskRepository.save(task2);

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("status", "TO_DO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].status").value("TO_DO"));
    }

    @Test
    void updateTask_ExistingId_ReturnsUpdatedTask() throws Exception {
        Task task = Task.builder()
                .title("Original Title")
                .status(TaskStatus.TO_DO)
                .priority(TaskPriority.MED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Task savedTask = taskRepository.save(task);

        TaskRequestDto updateRequest = TaskRequestDto.builder()
                .title("Updated Title")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .build();

        mockMvc.perform(put("/api/tasks/{id}", savedTask.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void partialUpdateTask_ExistingId_ReturnsPartiallyUpdatedTask() throws Exception {
        Task task = Task.builder()
                .title("Original Title")
                .description("Original Description")
                .status(TaskStatus.TO_DO)
                .priority(TaskPriority.MED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Task savedTask = taskRepository.save(task);

        TaskRequestDto partialUpdate = TaskRequestDto.builder()
                .title("Updated Title")
                .build();

        mockMvc.perform(patch("/api/tasks/{id}", savedTask.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType("application/merge-patch+json")
                        .content(objectMapper.writeValueAsString(partialUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.description").value("Original Description"));
    }

    @Test
    void deleteTask_ExistingId_ReturnsNoContent() throws Exception {
        Task task = Task.builder()
                .title("Task to Delete")
                .status(TaskStatus.TO_DO)
                .priority(TaskPriority.MED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Task savedTask = taskRepository.save(task);

        mockMvc.perform(delete("/api/tasks/{id}", savedTask.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        assertFalse(taskRepository.existsById(savedTask.getId()));
    }

    @Test
    void createTask_WithoutAuth_ReturnsUnauthorized() throws Exception {
        TaskRequestDto request = TaskRequestDto.builder()
                .title("Test Task")
                .build();

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}

