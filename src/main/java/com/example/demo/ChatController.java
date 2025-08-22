package com.example.demo;

import com.example.demo.model.ChatRequest;
import com.example.demo.model.ChatResponse;
import com.example.demo.model.ChatMessage;
import com.example.demo.service.ChatService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import reactor.core.publisher.Flux;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // 메인 채팅 UI 페이지
    @GetMapping("/")
    public String index() {
        return "chat";
    }

    // 모든 HTTP 메소드를 처리하여 디버깅
    @RequestMapping(value = "/api/chat", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS})
    @ResponseBody
    @CrossOrigin(origins = "*")
    public ResponseEntity<ChatMessageDto> handleChatRequest(
            HttpServletRequest httpRequest,
            @RequestBody(required = false) ChatMessageDto message) {
        
        // 요청 정보 상세 로깅
        logger.info("=== HTTP 요청 디버깅 정보 ===");
        logger.info("요청 메소드: {}", httpRequest.getMethod());
        logger.info("요청 URL: {}", httpRequest.getRequestURL());
        logger.info("요청 URI: {}", httpRequest.getRequestURI());
        logger.info("Content-Type: {}", httpRequest.getContentType());
        logger.info("Content-Length: {}", httpRequest.getContentLength());
        
        // 요청 바디 로깅
        logger.info("요청 바디 메시지: {}", message != null ? message.getMessage() : "null");
        
        // GET 요청인 경우 메소드 안내
        if ("GET".equals(httpRequest.getMethod())) {
            logger.info("GET 요청은 지원하지 않습니다.");
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ChatMessageDto("시스템", "GET 메소드는 지원하지 않습니다. POST 메소드를 사용해주세요."));
        }
        
        // POST가 아닌 경우 405 반환
        if (!"POST".equals(httpRequest.getMethod())) {
            logger.warn("지원하지 않는 HTTP 메소드: {}", httpRequest.getMethod());
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ChatMessageDto("시스템", "지원하지 않는 HTTP 메소드입니다: " + httpRequest.getMethod()));
        }
        
        logger.info("POST 요청 처리 시작 - 메시지: {}", message != null ? message.getMessage() : "null");
        logger.info("시스템 프롬프트: {}", message != null ? message.getSystemPrompt() : "null");
        
        try {
            // 입력값 검증
            if (message == null || message.getMessage() == null || message.getMessage().trim().isEmpty()) {
                logger.warn("빈 메시지 요청");
                return ResponseEntity.badRequest()
                    .body(new ChatMessageDto("AI", "메시지를 입력해주세요."));
            }
            
            // Convert to ChatRequest with system prompt
            ChatRequest chatRequest = new ChatRequest(message.getMessage());
            if (message.getSystemPrompt() != null && !message.getSystemPrompt().trim().isEmpty()) {
                chatRequest.setSystemPrompt(message.getSystemPrompt());
                logger.info("시스템 프롬프트가 설정됨: {}", message.getSystemPrompt());
            }
            
            // Get or create session ID
            HttpSession session = httpRequest.getSession();
            String sessionId = (String) session.getAttribute("chatSessionId");
            if (sessionId == null) {
                sessionId = UUID.randomUUID().toString();
                session.setAttribute("chatSessionId", sessionId);
            }
            
            // Call ChatService
            ChatResponse response = chatService.chat(chatRequest, sessionId);
            
            logger.info("AI 응답 생성 완료");
            return ResponseEntity.ok(new ChatMessageDto("AI", response.getMessage()));
            
        } catch (Exception e) {
            logger.error("AI 응답 생성 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ChatMessageDto("AI", "죄송합니다. 응답을 생성하는 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    
    // New REST API endpoints using ChatService
    
    /**
     * Chat endpoint with full request/response models
     */
    @PostMapping("/api/v1/chat")
    @ResponseBody
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request, HttpSession session) {
        try {
            // Validate request
            if (!chatService.isValidRequest(request)) {
                return ResponseEntity.badRequest()
                    .body(ChatResponse.builder()
                        .message("Invalid request: message is required")
                        .error(true)
                        .build());
            }
            
            // Get or create session ID if not provided
            String sessionId = request.getSessionId();
            if (sessionId == null) {
                sessionId = (String) session.getAttribute("chatSessionId");
                if (sessionId == null) {
                    sessionId = UUID.randomUUID().toString();
                    session.setAttribute("chatSessionId", sessionId);
                }
            }
            
            // Process chat request
            ChatResponse response = chatService.chat(request, sessionId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error in chat endpoint: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ChatResponse.builder()
                    .message("An error occurred: " + e.getMessage())
                    .error(true)
                    .build());
        }
    }
    
    /**
     * Streaming chat endpoint
     */
    @GetMapping(value = "/api/v1/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public Flux<String> streamChat(@RequestParam String message, HttpSession session) {
        try {
            // Get or create session ID
            String sessionId = (String) session.getAttribute("chatSessionId");
            if (sessionId == null) {
                sessionId = UUID.randomUUID().toString();
                session.setAttribute("chatSessionId", sessionId);
            }
            
            ChatRequest request = new ChatRequest(message);
            return chatService.streamChat(request, sessionId)
                .delayElements(Duration.ofMillis(50)); // Add slight delay for streaming effect
                
        } catch (Exception e) {
            logger.error("Error in streaming chat: ", e);
            return Flux.just("Error: " + e.getMessage());
        }
    }
    
    /**
     * Get chat history for current session
     */
    @GetMapping("/api/v1/chat/history")
    @ResponseBody
    public ResponseEntity<List<ChatMessage>> getChatHistory(HttpSession session) {
        String sessionId = (String) session.getAttribute("chatSessionId");
        if (sessionId == null) {
            return ResponseEntity.ok(List.of());
        }
        
        List<ChatMessage> history = chatService.getChatHistory(sessionId);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Clear chat history for current session
     */
    @DeleteMapping("/api/v1/chat/history")
    @ResponseBody
    public ResponseEntity<Map<String, String>> clearChatHistory(HttpSession session) {
        String sessionId = (String) session.getAttribute("chatSessionId");
        if (sessionId != null) {
            chatService.clearChatHistory(sessionId);
            session.removeAttribute("chatSessionId");
        }
        
        return ResponseEntity.ok(Map.of("status", "success", "message", "Chat history cleared"));
    }
    
    /**
     * Get available models
     */
    @GetMapping("/api/v1/models")
    @ResponseBody
    public ResponseEntity<List<String>> getAvailableModels() {
        List<String> models = chatService.getAvailableModels();
        return ResponseEntity.ok(models);
    }

    // Legacy ChatMessage DTO for backward compatibility
    public static class ChatMessageDto {
        private String sender;
        private String message;
        private String systemPrompt;  // 시스템 프롬프트 추가

        public ChatMessageDto() {}

        public ChatMessageDto(String sender, String message) {
            this.sender = sender;
            this.message = message;
        }

        public ChatMessageDto(String sender, String message, String systemPrompt) {
            this.sender = sender;
            this.message = message;
            this.systemPrompt = systemPrompt;
        }

        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getSystemPrompt() {
            return systemPrompt;
        }

        public void setSystemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
        }
    }
}
