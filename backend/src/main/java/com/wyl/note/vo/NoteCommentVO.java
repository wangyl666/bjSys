package com.wyl.note.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class NoteCommentVO {

    private Long id;

    private Long noteId;

    private Long userId;

    private String username;

    private String userAvatar;

    private Long parentId;

    private Long replyToUserId;

    private String replyToUsername;

    private String content;

    private List<NoteCommentVO> children;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
