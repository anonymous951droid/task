package com.kanban.mapper;

import com.kanban.dto.TaskRequestDto;
import com.kanban.dto.TaskResponseDto;
import com.kanban.entity.Task;
import com.kanban.model.TaskPriority;
import com.kanban.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskMapperTest {

    private TaskMapper taskMapper;

    @BeforeEach
    void setUp() {
        taskMapper = Mappers.getMapper(TaskMapper.class);
    }

    @Test
    void toEntity_ValidDto_ReturnsEntity() {
        // Given
        TaskRequestDto dto = TaskRequestDto.builder()
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .build();

        // When
        Task entity = taskMapper.toEntity(dto);

        // Then
        assertNotNull(entity);
        assertEquals("Test Task", entity.getTitle());
        assertEquals("Test Description", entity.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, entity.getStatus());
        assertEquals(TaskPriority.HIGH, entity.getPriority());
        assertNull(entity.getId());
        assertNull(entity.getVersion());
    }

    @Test
    void toEntity_NullStatus_DefaultsToToDo() {
        // Given
        TaskRequestDto dto = TaskRequestDto.builder()
                .title("Test Task")
                .status(null)
                .build();

        // When
        Task entity = taskMapper.toEntity(dto);

        // Then
        assertEquals(TaskStatus.TO_DO, entity.getStatus());
    }

    @Test
    void toEntity_NullPriority_DefaultsToMed() {
        // Given
        TaskRequestDto dto = TaskRequestDto.builder()
                .title("Test Task")
                .priority(null)
                .build();

        // When
        Task entity = taskMapper.toEntity(dto);

        // Then
        assertEquals(TaskPriority.MED, entity.getPriority());
    }

    @Test
    void toDto_ValidEntity_ReturnsDto() {
        // Given
        Task entity = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.DONE)
                .priority(TaskPriority.LOW)
                .version(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // When
        TaskResponseDto dto = taskMapper.toDto(entity);

        // Then
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Test Task", dto.getTitle());
        assertEquals("Test Description", dto.getDescription());
        assertEquals(TaskStatus.DONE, dto.getStatus());
        assertEquals(TaskPriority.LOW, dto.getPriority());
        assertEquals(1L, dto.getVersion());
        assertNotNull(dto.getCreatedAt());
        assertNotNull(dto.getUpdatedAt());
    }

    @Test
    void updateEntityFromDto_ValidDto_UpdatesEntity() {
        // Given
        Task entity = Task.builder()
                .id(1L)
                .title("Old Title")
                .description("Old Description")
                .status(TaskStatus.TO_DO)
                .priority(TaskPriority.MED)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        TaskRequestDto dto = TaskRequestDto.builder()
                .title("New Title")
                .description("New Description")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .build();

        // When
        taskMapper.updateEntityFromDto(dto, entity);

        // Then
        assertEquals("New Title", entity.getTitle());
        assertEquals("New Description", entity.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, entity.getStatus());
        assertEquals(TaskPriority.HIGH, entity.getPriority());
        assertEquals(1L, entity.getId()); // ID should not change
        assertEquals(0L, entity.getVersion()); // Version should not change
    }
}

