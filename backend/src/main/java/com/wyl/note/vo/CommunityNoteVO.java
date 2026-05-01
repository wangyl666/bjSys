package com.wyl.note.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommunityNoteVO {

    private Long id;

    private Long userId;

    private String username;

    private String userAvatar;

    private Long categoryId;

    private String categoryName;

    private String title;

    private String content;

    private String summary;

    private Integer viewCount;

    private Long favoriteCount;

    private Long commentCount;

    private Boolean isFavorited;

    private List<TagVO> tags;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
