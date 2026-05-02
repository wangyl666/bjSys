package com.wyl.note.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wyl.note.service.ChatMessageService;
import com.wyl.note.vo.ChatMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    private final ObjectMapper objectMapper;

    private final ChatMessageService chatMessageService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        String username = (String) session.getAttributes().get("username");
        
        sessions.put(userId, session);
        log.info("用户 {} (ID: {}) 加入聊天室，当前在线人数: {}", username, userId, sessions.size());
        
        sendSystemMessage("用户 " + username + " 加入了聊天室");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        String username = (String) session.getAttributes().get("username");
        
        String payload = message.getPayload();
        log.info("收到来自用户 {} 的消息: {}", username, payload);
        
        try {
            MessageDto messageDto = objectMapper.readValue(payload, MessageDto.class);
            
            if ("TEXT".equals(messageDto.getType())) {
                ChatMessageVO savedMessage = chatMessageService.saveMessage(
                        userId, 
                        messageDto.getContent(), 
                        "TEXT"
                );
                
                broadcastMessage(savedMessage);
            }
        } catch (Exception e) {
            log.error("处理消息失败", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        String username = (String) session.getAttributes().get("username");
        
        sessions.remove(userId);
        log.info("用户 {} (ID: {}) 离开聊天室，当前在线人数: {}", username, userId, sessions.size());
        
        sendSystemMessage("用户 " + username + " 离开了聊天室");
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误", exception);
        session.close(CloseStatus.SERVER_ERROR);
    }

    private void broadcastMessage(ChatMessageVO message) {
        String jsonMessage;
        try {
            jsonMessage = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("序列化消息失败", e);
            return;
        }
        
        TextMessage textMessage = new TextMessage(jsonMessage);
        
        for (Map.Entry<Long, WebSocketSession> entry : sessions.entrySet()) {
            WebSocketSession session = entry.getValue();
            if (session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                } catch (IOException e) {
                    log.error("发送消息给用户 {} 失败", entry.getKey(), e);
                }
            }
        }
    }

    private void sendSystemMessage(String content) {
        ChatMessageVO systemMessage = new ChatMessageVO();
        systemMessage.setId(0L);
        systemMessage.setUserId(0L);
        systemMessage.setContent(content);
        systemMessage.setMessageType("SYSTEM");
        systemMessage.setCreatedAt(java.time.LocalDateTime.now());
        
        broadcastMessage(systemMessage);
    }

    @lombok.Data
    public static class MessageDto {
        private String type;
        private String content;
    }
}
