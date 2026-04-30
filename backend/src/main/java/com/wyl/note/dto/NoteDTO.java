package com.wyl.note.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class NoteDTO {

    private Long id;

    private Long categoryId;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String content;

    private String summary;

    private Integer isPublic;

    private List<Long> tagIds;
}
