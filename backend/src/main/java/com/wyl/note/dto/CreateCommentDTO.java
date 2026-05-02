package com.wyl.note.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CreateCommentDTO {

    @NotNull(message = "笔记ID不能为空")
    private Long noteId;

    private Long parentId;

    private Long replyToUserId;

    @NotBlank(message = "评论内容不能为空")
    private String content;
}
