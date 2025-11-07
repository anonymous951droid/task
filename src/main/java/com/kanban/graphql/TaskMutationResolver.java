package com.kanban.graphql;

import com.kanban.dto.TaskRequestDto;
import com.kanban.dto.TaskResponseDto;
import com.kanban.service.TaskService;
import com.kanban.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class TaskMutationResolver {

    private final TaskService taskService;
    private final WebSocketNotificationService webSocketNotificationService;

    @MutationMapping
    public TaskResponseDto createTask(@Argument("input") TaskInput input) {
        TaskRequestDto requestDto = TaskRequestDto.builder()
                .title(input.title())
                .description(input.description())
                .status(input.status())
                .priority(input.priority())
                .build();
        TaskResponseDto createdTask = taskService.createTask(requestDto);
        webSocketNotificationService.notifyTaskCreated(createdTask);
        return createdTask;
    }

    @MutationMapping
    public TaskResponseDto updateTask(
            @Argument Long id,
            @Argument("input") TaskInput input) {
        TaskRequestDto requestDto = TaskRequestDto.builder()
                .title(input.title())
                .description(input.description())
                .status(input.status())
                .priority(input.priority())
                .build();
        TaskResponseDto updatedTask = taskService.updateTask(id, requestDto);
        webSocketNotificationService.notifyTaskUpdated(updatedTask);
        return updatedTask;
    }

    @MutationMapping
    public TaskResponseDto partialUpdateTask(
            @Argument Long id,
            @Argument("input") TaskPartialInput input) {
        TaskRequestDto requestDto = TaskRequestDto.builder()
                .title(input.title())
                .description(input.description())
                .status(input.status())
                .priority(input.priority())
                .build();
        TaskResponseDto updatedTask = taskService.partialUpdateTask(id, requestDto);
        webSocketNotificationService.notifyTaskUpdated(updatedTask);
        return updatedTask;
    }

    @MutationMapping
    public Boolean deleteTask(@Argument Long id) {
        taskService.deleteTask(id);
        webSocketNotificationService.notifyTaskDeleted(id);
        return true;
    }
}

