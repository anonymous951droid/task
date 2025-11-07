package com.kanban.graphql;

import com.kanban.model.TaskPriority;
import com.kanban.model.TaskStatus;

public record TaskPartialInput(
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority
) {}

