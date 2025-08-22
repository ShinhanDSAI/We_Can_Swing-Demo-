package com.example.demo.service;

import com.example.demo.OpenRouterChatModel;
import com.example.demo.model.ChatRequest;
import com.example.demo.model.ChatResponse;
import com.example.demo.model.ChatMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ChatService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    
    // Chat history storage (session-based)
    private final Map<String, List<ChatMessage>> chatHistories = new ConcurrentHashMap<>();
    
    private final OpenRouterChatModel chatModel;
    
    @Value("${spring.ai.openai.chat.options.model:gpt-3.5-turbo}")
    private String defaultModel;
    
    @Value("${spring.ai.openai.chat.options.temperature:0.7}")
    private Double temperature;
    
    @Value("${spring.ai.openai.chat.options.max-tokens:2000}")
    private Integer maxTokens;
    
    // System prompt for chat context
    private static final String DEFAULT_SYSTEM_PROMPT = 
        "You are a helpful AI assistant. Please provide clear, accurate, and helpful responses.";
    
    public ChatService(OpenRouterChatModel chatModel) {
        this.chatModel = chatModel;
        logger.info("ChatService initialized with OpenRouterChatModel");
    }
    
    /**
     * Process a chat request and return a response
     * @param request The chat request containing message and optional parameters
     * @return ChatResponse with the AI's response
     */
    public ChatResponse chat(ChatRequest request) {
        return chat(request, null);
    }
    
    /**
     * Process a chat request with session support
     * @param request The chat request
     * @param sessionId Optional session ID for maintaining conversation history
     * @return ChatResponse with the AI's response
     */
    public ChatResponse chat(ChatRequest request, String sessionId) {
        try {
            logger.info("=== 채팅 요청 처리 시작 ===");
            logger.info("사용자 메시지: {}", request.getMessage());
            logger.info("세션 ID: {}", sessionId);
            logger.info("요청 모델: {}", request.getModel() != null ? request.getModel() : defaultModel);
            
            // Build the prompt with conversation history if available
            Prompt prompt = buildPrompt(request, sessionId);
            
            // 프롬프트 내용 로깅
            logger.info("=== 생성된 프롬프트 ===");
            logger.info("프롬프트 메시지 개수: {}", prompt.getInstructions().size());
            
            // Call the AI model
            logger.info("AI 모델 호출 중...");
            org.springframework.ai.chat.model.ChatResponse aiResponse = chatModel.call(prompt);
            
            String responseText = aiResponse.getResult().getOutput().getText();
            logger.info("AI 응답 생성 완료: {}",
                responseText.length() > 100 ? responseText.substring(0, 100) + "..." : responseText);
            
            // Store conversation history if session is provided
            if (sessionId != null) {
                addToHistory(sessionId, request.getMessage(), responseText);
            }
            
            // Create and return response
            return ChatResponse.builder()
                .message(responseText)
                .model(request.getModel() != null ? request.getModel() : defaultModel)
                .sessionId(sessionId)
                .timestamp(new Date())
                .build();
                
        } catch (Exception e) {
            logger.error("Error processing chat request: ", e);
            return ChatResponse.builder()
                .message("Sorry, I encountered an error processing your request: " + e.getMessage())
                .error(true)
                .timestamp(new Date())
                .build();
        }
    }
    
    /**
     * Stream chat responses (for real-time streaming)
     * @param request The chat request
     * @param sessionId Optional session ID
     * @return Flux of response chunks
     */
    public Flux<String> streamChat(ChatRequest request, String sessionId) {
        try {
            logger.info("Starting streaming chat - Message: {}", request.getMessage());
            
            // Build the prompt
            Prompt prompt = buildPrompt(request, sessionId);
            
            // For now, return a single response as streaming is not directly supported
            // This can be enhanced when streaming is available in the model
            org.springframework.ai.chat.model.ChatResponse aiResponse = chatModel.call(prompt);
            String response = aiResponse.getResult().getOutput().getText();
            
            // Store in history
            if (sessionId != null) {
                addToHistory(sessionId, request.getMessage(), response);
            }
            
            // Simulate streaming by breaking response into chunks
            return Flux.fromIterable(Arrays.asList(response.split(" ")))
                .map(word -> word + " ");
                
        } catch (Exception e) {
            logger.error("Error in streaming chat: ", e);
            return Flux.just("Error: " + e.getMessage());
        }
    }
    
    /**
     * Get chat history for a session
     * @param sessionId The session ID
     * @return List of chat messages in the session
     */
    public List<ChatMessage> getChatHistory(String sessionId) {
        return chatHistories.getOrDefault(sessionId, new ArrayList<>());
    }
    
    /**
     * Clear chat history for a session
     * @param sessionId The session ID
     */
    public void clearChatHistory(String sessionId) {
        chatHistories.remove(sessionId);
        logger.info("Cleared chat history for session: {}", sessionId);
    }
    
    /**
     * Build a prompt with conversation context
     */
    private Prompt buildPrompt(ChatRequest request, String sessionId) {
        List<Message> messages = new ArrayList<>();
        
        // Add system message
        String systemPrompt = request.getSystemPrompt() != null ?
            request.getSystemPrompt() : DEFAULT_SYSTEM_PROMPT;
        messages.add(new SystemMessage(systemPrompt));
        logger.debug("시스템 프롬프트 추가: {}", systemPrompt);
        
        // Add conversation history if available
        if (sessionId != null) {
            List<ChatMessage> history = getChatHistory(sessionId);
            logger.debug("대화 히스토리 개수: {}", history.size());
            for (ChatMessage msg : history) {
                if ("user".equalsIgnoreCase(msg.getRole())) {
                    messages.add(new UserMessage(msg.getContent()));
                    logger.debug("히스토리 - 사용자: {}", msg.getContent());
                } else if ("assistant".equalsIgnoreCase(msg.getRole())) {
                    messages.add(new AssistantMessage(msg.getContent()));
                    logger.debug("히스토리 - AI: {}", msg.getContent());
                }
            }
        }
        
        // Add current user message - 이 부분이 중요: 사용자의 현재 메시지가 프롬프트에 포함됨
        messages.add(new UserMessage(request.getMessage()));
        logger.info("현재 사용자 메시지를 프롬프트에 추가: {}", request.getMessage());
        
        return new Prompt(messages);
    }
    
    /**
     * Add messages to chat history
     */
    private void addToHistory(String sessionId, String userMessage, String assistantMessage) {
        List<ChatMessage> history = chatHistories.computeIfAbsent(sessionId, k -> new ArrayList<>());
        
        // Add user message
        history.add(ChatMessage.builder()
            .role("user")
            .content(userMessage)
            .timestamp(new Date())
            .build());
        
        // Add assistant message
        history.add(ChatMessage.builder()
            .role("assistant")
            .content(assistantMessage)
            .timestamp(new Date())
            .build());
        
        // Limit history size to prevent memory issues (keep last 20 messages)
        if (history.size() > 20) {
            history.subList(0, history.size() - 20).clear();
        }
    }
    
    /**
     * Get available models (for future multi-model support)
     * @return List of available model names
     */
    public List<String> getAvailableModels() {
        return Arrays.asList(
            "gpt-3.5-turbo",
            "gpt-4",
            "claude-3-opus",
            "claude-3-sonnet",
            "gemini-pro",
            "mistral-medium"
        );
    }
    
    /**
     * Validate chat request
     */
    public boolean isValidRequest(ChatRequest request) {
        return request != null && 
               request.getMessage() != null && 
               !request.getMessage().trim().isEmpty();
    }
}