package com.wyl.note.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("note_approval")
public class NoteApproval {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long noteId;

    private Long userId;

    private String approvalStatus;

    private Long adminId;

    private String adminRemark;

    private LocalDateTime submittedAt;

    private LocalDateTime approvedAt;
}
