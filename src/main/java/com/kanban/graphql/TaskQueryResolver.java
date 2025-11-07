package com.kanban.graphql;

import com.kanban.dto.TaskResponseDto;
import com.kanban.model.TaskStatus;
import com.kanban.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class TaskQueryResolver {

    private final TaskService taskService;

    @QueryMapping
    public TaskPage tasks(
            @Argument TaskStatus status,
            @Argument Integer page,
            @Argument Integer size,
            @Argument List<String> sort) {
        
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? size : 20;
        
        Pageable pageable;
        if (sort != null && !sort.isEmpty()) {
            List<Sort.Order> orders = new ArrayList<>();
            for (String sortParam : sort) {
                String[] parts = sortParam.split(",");
                String field = parts[0];
                Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;
                orders.add(new Sort.Order(direction, field));
            }
            pageable = PageRequest.of(pageNumber, pageSize, Sort.by(orders));
        } else {
            pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").descending());
        }
        
        Page<TaskResponseDto> taskPage = taskService.getAllTasks(status, pageable);
        return TaskPage.from(taskPage);
    }

    @QueryMapping
    public TaskResponseDto task(@Argument Long id) {
        return taskService.getTaskById(id);
    }
}

