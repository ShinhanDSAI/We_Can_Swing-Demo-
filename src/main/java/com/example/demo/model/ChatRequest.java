package com.example.demo.model;

public class ChatRequest {
    private String message;
    private String sender;
    private String model;
    private String systemPrompt;
    private Double temperature;
    private Integer maxTokens;
    private String sessionId;

    // Default constructor
    public ChatRequest() {}

    // Constructor with message only
    public ChatRequest(String message) {
        this.message = message;
    }

    // Constructor with message and sender
    public ChatRequest(String message, String sender) {
        this.message = message;
        this.sender = sender;
    }

    // Full constructor
    public ChatRequest(String message, String sender, String model, String systemPrompt,
                      Double temperature, Integer maxTokens, String sessionId) {
        this.message = message;
        this.sender = sender;
        this.model = model;
        this.systemPrompt = systemPrompt;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.sessionId = sessionId;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        return "ChatRequest{" +
                "message='" + message + '\'' +
                ", sender='" + sender + '\'' +
                ", model='" + model + '\'' +
                ", systemPrompt='" + systemPrompt + '\'' +
                ", temperature=" + temperature +
                ", maxTokens=" + maxTokens +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}