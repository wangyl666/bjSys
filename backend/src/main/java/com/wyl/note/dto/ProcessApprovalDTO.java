package com.wyl.note.dto;

import lombok.Data;

@Data
public class ProcessApprovalDTO {

    private Long approvalId;

    private Boolean approved;

    private String remark;
}
