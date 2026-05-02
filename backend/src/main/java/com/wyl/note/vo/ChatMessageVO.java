package com.wyl.note.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageVO {

    private Long id;

    private Long userId;

    private String content;

    private String messageType;

    private LocalDateTime createdAt;

    private UserVO user;
}
