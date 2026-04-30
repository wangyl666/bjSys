package com.wyl.note.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Draft implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long noteId;

    private String title;

    private String content;

    private Long categoryId;

    private List<Long> tagIds;

    private LocalDateTime savedAt;
}
