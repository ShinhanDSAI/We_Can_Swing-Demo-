package com.example.demo.model;

import java.util.Date;

public class ChatResponse {
    private String message;
    private String model;
    private String sessionId;
    private Date timestamp;
    private boolean error;
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String message;
        private String model;
        private String sessionId;
        private Date timestamp;
        private boolean error = false;
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder model(String model) {
            this.model = model;
            return this;
        }
        
        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }
        
        public Builder timestamp(Date timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder error(boolean error) {
            this.error = error;
            return this;
        }
        
        public ChatResponse build() {
            ChatResponse response = new ChatResponse();
            response.message = this.message;
            response.model = this.model;
            response.sessionId = this.sessionId;
            response.timestamp = this.timestamp;
            response.error = this.error;
            return response;
        }
    }
    
    // Default constructor
    public ChatResponse() {}
    
    // Constructor with message only
    public ChatResponse(String message) {
        this.message = message;
        this.timestamp = new Date();
    }
    
    // Full constructor
    public ChatResponse(String message, String model, String sessionId, Date timestamp, boolean error) {
        this.message = message;
        this.model = model;
        this.sessionId = sessionId;
        this.timestamp = timestamp;
        this.error = error;
    }
    
    // Getters and Setters
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    public boolean isError() {
        return error;
    }
    
    public void setError(boolean error) {
        this.error = error;
    }
    
    @Override
    public String toString() {
        return "ChatResponse{" +
                "message='" + message + '\'' +
                ", model='" + model + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", timestamp=" + timestamp +
                ", error=" + error +
                '}';
    }
}