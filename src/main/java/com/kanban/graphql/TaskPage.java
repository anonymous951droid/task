package com.kanban.graphql;

import com.kanban.dto.TaskResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;

public record TaskPage(
        List<TaskResponseDto> content,
        Long totalElements,
        Integer totalPages,
        Integer number,
        Integer size,
        Integer numberOfElements,
        Boolean first,
        Boolean last
) {
    public static TaskPage from(Page<TaskResponseDto> page) {
        return new TaskPage(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.getNumberOfElements(),
                page.isFirst(),
                page.isLast()
        );
    }
}

