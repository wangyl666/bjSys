package com.wyl.note.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoteApprovalVO {

    private Long id;

    private Long noteId;

    private String noteTitle;

    private Long userId;

    private String username;

    private String approvalStatus;

    private Long adminId;

    private String adminName;

    private String adminRemark;

    private LocalDateTime submittedAt;

    private LocalDateTime approvedAt;
}
