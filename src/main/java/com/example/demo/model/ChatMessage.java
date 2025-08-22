package com.example.demo.model;

import java.util.Date;

public class ChatMessage {
    private String role;      // "user", "assistant", "system"
    private String content;
    private Date timestamp;
    private String messageId;
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String role;
        private String content;
        private Date timestamp;
        private String messageId;
        
        public Builder role(String role) {
            this.role = role;
            return this;
        }
        
        public Builder content(String content) {
            this.content = content;
            return this;
        }
        
        public Builder timestamp(Date timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }
        
        public ChatMessage build() {
            ChatMessage message = new ChatMessage();
            message.role = this.role;
            message.content = this.content;
            message.timestamp = this.timestamp != null ? this.timestamp : new Date();
            message.messageId = this.messageId;
            return message;
        }
    }
    
    // Default constructor
    public ChatMessage() {}
    
    // Constructor with role and content
    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
        this.timestamp = new Date();
    }
    
    // Full constructor
    public ChatMessage(String role, String content, Date timestamp, String messageId) {
        this.role = role;
        this.content = content;
        this.timestamp = timestamp;
        this.messageId = messageId;
    }
    
    // Getters and Setters
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getMessageId() {
        return messageId;
    }
    
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    @Override
    public String toString() {
        return "ChatMessage{" +
                "role='" + role + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", messageId='" + messageId + '\'' +
                '}';
    }
}