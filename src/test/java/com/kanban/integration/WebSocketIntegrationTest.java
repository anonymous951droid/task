package com.kanban.integration;

import com.kanban.dto.TaskRequestDto;
import com.kanban.dto.TaskResponseDto;
import com.kanban.model.TaskPriority;
import com.kanban.model.TaskStatus;
import com.kanban.service.TaskService;
import com.kanban.service.WebSocketNotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.web.socket.sockjs.client.Transport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class WebSocketIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("kanban_test_db")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private TaskService taskService;

    @Autowired
    private WebSocketNotificationService webSocketNotificationService;

    @Value("${local.server.port}")
    private int serverPort;

    private WebSocketStompClient stompClient;
    private StompSession stompSession;
    private BlockingQueue<Object> messages;

    @BeforeEach
    void setUp() throws Exception {
        List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);
        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        messages = new LinkedBlockingQueue<>();
        
        String url = "http://localhost:" + serverPort + "/ws";
        stompSession = stompClient.connect(url, new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);
        
        stompSession.subscribe("/topic/tasks", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Object.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messages.offer(payload);
            }
        });
    }

    @AfterEach
    void tearDown() throws Exception {
        if (stompSession != null && stompSession.isConnected()) {
            stompSession.disconnect();
        }
        if (stompClient != null) {
            try {
                stompClient.stop();
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }

    @Test
    void createTask_EmitsWebSocketEvent() throws Exception {
        // Given
        TaskRequestDto request = TaskRequestDto.builder()
                .title("WebSocket Test Task")
                .description("Test Description")
                .status(TaskStatus.TO_DO)
                .priority(TaskPriority.MED)
                .build();

        // When
        TaskResponseDto createdTask = taskService.createTask(request);
        webSocketNotificationService.notifyTaskCreated(createdTask);

        // Then
        Object message = messages.poll(5, TimeUnit.SECONDS);
        assertNotNull(message, "WebSocket message should be received");
    }

    @Test
    void updateTask_EmitsWebSocketEvent() throws Exception {
        // Given
        TaskRequestDto createRequest = TaskRequestDto.builder()
                .title("Task to Update")
                .status(TaskStatus.TO_DO)
                .priority(TaskPriority.MED)
                .build();
        TaskResponseDto createdTask = taskService.createTask(createRequest);

        TaskRequestDto updateRequest = TaskRequestDto.builder()
                .title("Updated Task")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .build();

        // When
        TaskResponseDto updatedTask = taskService.updateTask(createdTask.getId(), updateRequest);
        webSocketNotificationService.notifyTaskUpdated(updatedTask);

        // Then
        Object message = messages.poll(5, TimeUnit.SECONDS);
        assertNotNull(message, "WebSocket message should be received");
    }

    @Test
    void deleteTask_EmitsWebSocketEvent() throws Exception {
        // Given
        TaskRequestDto createRequest = TaskRequestDto.builder()
                .title("Task to Delete")
                .status(TaskStatus.TO_DO)
                .priority(TaskPriority.MED)
                .build();
        TaskResponseDto createdTask = taskService.createTask(createRequest);

        // When
        taskService.deleteTask(createdTask.getId());
        webSocketNotificationService.notifyTaskDeleted(createdTask.getId());

        // Then
        Object message = messages.poll(5, TimeUnit.SECONDS);
        assertNotNull(message, "WebSocket message should be received");
    }
}

