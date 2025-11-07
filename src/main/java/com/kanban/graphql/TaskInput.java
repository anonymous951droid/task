package com.kanban.graphql;

import com.kanban.model.TaskPriority;
import com.kanban.model.TaskStatus;

public record TaskInput(
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority
) {}

