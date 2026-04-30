package com.wyl.note.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wyl.note.entity.NoteApproval;
import com.wyl.note.vo.NoteApprovalVO;

public interface NoteApprovalService extends IService<NoteApproval> {

    NoteApprovalVO submitApproval(Long noteId, Long userId);

    NoteApprovalVO processApproval(Long approvalId, Long adminId, Boolean approved, String remark);

    Page<NoteApprovalVO> getMyApprovals(Long userId, Integer page, Integer size);

    Page<NoteApprovalVO> getPendingApprovals(Integer page, Integer size);

    NoteApprovalVO getApprovalDetail(Long approvalId, Long userId, String role);

    NoteApprovalVO viewApproval(Long approvalId, Long adminId);
}
