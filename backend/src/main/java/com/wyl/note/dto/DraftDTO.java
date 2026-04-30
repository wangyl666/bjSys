package com.wyl.note.dto;

import lombok.Data;

import java.util.List;

@Data
public class DraftDTO {

    private Long noteId;

    private Long categoryId;

    private String title;

    private String content;

    private List<Long> tagIds;
}
