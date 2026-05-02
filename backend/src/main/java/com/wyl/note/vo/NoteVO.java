package com.wyl.note.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NoteVO {

    private Long id;

    private Long userId;

    private Long categoryId;

    private String categoryName;

    private String title;

    private String content;

    private String summary;

    private Integer isPublic;

    private String approvalStatus;

    private Integer viewCount;

    private List<TagVO> tags;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
