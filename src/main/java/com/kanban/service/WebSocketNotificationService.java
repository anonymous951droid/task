package com.kanban.service;

import com.kanban.dto.TaskResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private static final String TOPIC = "/topic/tasks";

    public void notifyTaskCreated(TaskResponseDto task) {
        messagingTemplate.convertAndSend(TOPIC, new TaskEvent("CREATED", task));
        log.info("Sent WebSocket notification for task creation: {}", task.getId());
    }

    public void notifyTaskUpdated(TaskResponseDto task) {
        messagingTemplate.convertAndSend(TOPIC, new TaskEvent("UPDATED", task));
        log.info("Sent WebSocket notification for task update: {}", task.getId());
    }

    public void notifyTaskDeleted(Long taskId) {
        messagingTemplate.convertAndSend(TOPIC, new TaskEvent("DELETED", taskId));
        log.info("Sent WebSocket notification for task deletion: {}", taskId);
    }

    public record TaskEvent(String eventType, Object payload) {}
}

