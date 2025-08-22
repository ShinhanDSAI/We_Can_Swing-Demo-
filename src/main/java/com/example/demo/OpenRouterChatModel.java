package com.example.demo;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Component
public class OpenRouterChatModel implements ChatModel {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenRouterChatModel.class);
    
    @Value("${spring.ai.openai.api-key}")
    private String apiKey;
    
    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;
    
    @Value("${spring.ai.openai.chat.options.model}")
    private String model;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Override
    public ChatResponse call(Prompt prompt) {
        try {
            logger.info("OpenRouter API 호출 시작");
            logger.info("프롬프트에 포함된 메시지 개수: {}", prompt.getInstructions().size());
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            // 요청 바디 구성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("temperature", 0.7);
            
            // 메시지 구성 - 모든 메시지를 처리
            List<Map<String, String>> messages = new ArrayList<>();
            
            // Prompt의 모든 메시지를 순회하며 추가
            prompt.getInstructions().forEach(instruction -> {
                Map<String, String> message = new HashMap<>();
                
                // 메시지 타입에 따라 role 설정
                String messageType = instruction.getClass().getSimpleName();
                logger.debug("메시지 타입: {}, 내용: {}", messageType,
                    instruction.getText().length() > 100 ?
                    instruction.getText().substring(0, 100) + "..." : instruction.getText());
                
                switch (messageType) {
                    case "SystemMessage":
                        message.put("role", "system");
                        break;
                    case "UserMessage":
                        message.put("role", "user");
                        break;
                    case "AssistantMessage":
                        message.put("role", "assistant");
                        break;
                    default:
                        message.put("role", "user");
                        break;
                }
                
                message.put("content", instruction.getText());
                messages.add(message);
            });
            
            requestBody.put("messages", messages);
            
            logger.info("OpenRouter로 전송할 메시지 개수: {}", messages.size());
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            logger.info("OpenRouter 요청 - URL: {}/chat/completions", baseUrl);
            logger.info("OpenRouter 요청 - Model: {}", model);
            
            // API 호출
            ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/chat/completions", entity, Map.class
            );
            
            logger.info("OpenRouter 응답 수신: {}", response.getStatusCode());
            
            // 응답 파싱
            Map<String, Object> responseBody = response.getBody();
            List<?> choices = (List<?>) responseBody.get("choices");
            Map<String, Object> firstChoice = (Map<String, Object>) choices.get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            String content = (String) message.get("content");
            
            // ChatResponse 생성
            AssistantMessage assistantMessage = new AssistantMessage(content);
            Generation generation = new Generation(assistantMessage);
            return new ChatResponse(List.of(generation));
            
        } catch (Exception e) {
            logger.error("OpenRouter API 호출 실패: ", e);
            throw new RuntimeException("OpenRouter API 호출 실패: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String call(String message) {
        Prompt prompt = new Prompt(message);
        ChatResponse response = call(prompt);
        return response.getResult().getOutput().getText();
    }
}