package com.wyl.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyl.note.entity.Note;
import com.wyl.note.entity.NoteApproval;
import com.wyl.note.entity.User;
import com.wyl.note.mapper.NoteApprovalMapper;
import com.wyl.note.service.NoteApprovalService;
import com.wyl.note.service.NoteService;
import com.wyl.note.service.UserService;
import com.wyl.note.vo.NoteApprovalVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteApprovalServiceImpl extends ServiceImpl<NoteApprovalMapper, NoteApproval> implements NoteApprovalService {

    private final NoteService noteService;
    private final UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoteApprovalVO submitApproval(Long noteId, Long userId) {
        Note note = noteService.getById(noteId);
        if (note == null) {
            throw new RuntimeException("笔记不存在");
        }
        if (!note.getUserId().equals(userId)) {
            throw new RuntimeException("无权限操作此笔记");
        }
        if (note.getIsPublic() == 0) {
            throw new RuntimeException("只有公开笔记才能提交审批");
        }
        
        NoteApproval existingApproval = getOne(
                new LambdaQueryWrapper<NoteApproval>()
                        .eq(NoteApproval::getNoteId, noteId)
                        .eq(NoteApproval::getApprovalStatus, "PENDING")
        );
        if (existingApproval != null) {
            throw new RuntimeException("该笔记已在审批中");
        }
        
        note.setApprovalStatus("PENDING");
        noteService.updateById(note);
        
        NoteApproval approval = new NoteApproval();
        approval.setNoteId(noteId);
        approval.setUserId(userId);
        approval.setApprovalStatus("PENDING");
        approval.setSubmittedAt(LocalDateTime.now());
        save(approval);
        
        return convertToVO(approval);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoteApprovalVO processApproval(Long approvalId, Long adminId, Boolean approved, String remark) {
        NoteApproval approval = getById(approvalId);
        if (approval == null) {
            throw new RuntimeException("审批记录不存在");
        }
        if (!"PENDING".equals(approval.getApprovalStatus())) {
            throw new RuntimeException("该审批已被处理");
        }
        
        approval.setAdminId(adminId);
        approval.setAdminRemark(remark);
        approval.setApprovalStatus(approved ? "APPROVED" : "REJECTED");
        approval.setApprovedAt(LocalDateTime.now());
        updateById(approval);
        
        Note note = noteService.getById(approval.getNoteId());
        if (note != null) {
            note.setApprovalStatus(approved ? "APPROVED" : "REJECTED");
            noteService.updateById(note);
        }
        
        return convertToVO(approval);
    }

    @Override
    public Page<NoteApprovalVO> getMyApprovals(Long userId, Integer page, Integer size) {
        Page<NoteApproval> approvalPage = new Page<>(page, size);
        Page<NoteApproval> resultPage = page(approvalPage,
                new LambdaQueryWrapper<NoteApproval>()
                        .eq(NoteApproval::getUserId, userId)
                        .orderByDesc(NoteApproval::getSubmittedAt)
        );
        
        return convertToVOPage(resultPage);
    }

    @Override
    public Page<NoteApprovalVO> getPendingApprovals(Integer page, Integer size) {
        Page<NoteApproval> approvalPage = new Page<>(page, size);
        Page<NoteApproval> resultPage = page(approvalPage,
                new LambdaQueryWrapper<NoteApproval>()
                        .eq(NoteApproval::getApprovalStatus, "PENDING")
                        .orderByDesc(NoteApproval::getSubmittedAt)
        );
        
        return convertToVOPage(resultPage);
    }

    @Override
    public NoteApprovalVO getApprovalDetail(Long approvalId, Long userId, String role) {
        NoteApproval approval = getById(approvalId);
        if (approval == null) {
            throw new RuntimeException("审批记录不存在");
        }
        
        if (!"ADMIN".equals(role) && !approval.getUserId().equals(userId)) {
            throw new RuntimeException("无权限查看此审批记录");
        }
        
        return convertToVO(approval);
    }

    private NoteApprovalVO convertToVO(NoteApproval approval) {
        NoteApprovalVO vo = new NoteApprovalVO();
        BeanUtils.copyProperties(approval, vo);
        
        Note note = noteService.getById(approval.getNoteId());
        if (note != null) {
            vo.setNoteTitle(note.getTitle());
        }
        
        User user = userService.getById(approval.getUserId());
        if (user != null) {
            vo.setUsername(user.getNickname() != null ? user.getNickname() : user.getUsername());
        }
        
        if (approval.getAdminId() != null) {
            User admin = userService.getById(approval.getAdminId());
            if (admin != null) {
                vo.setAdminName(admin.getNickname() != null ? admin.getNickname() : admin.getUsername());
            }
        }
        
        return vo;
    }

    private Page<NoteApprovalVO> convertToVOPage(Page<NoteApproval> approvalPage) {
        Page<NoteApprovalVO> voPage = new Page<>(approvalPage.getCurrent(), approvalPage.getSize(), approvalPage.getTotal());
        
        List<NoteApproval> records = approvalPage.getRecords();
        if (records.isEmpty()) {
            voPage.setRecords(new ArrayList<>());
            return voPage;
        }
        
        List<Long> noteIds = records.stream().map(NoteApproval::getNoteId).distinct().collect(Collectors.toList());
        List<Long> userIds = records.stream().map(NoteApproval::getUserId).distinct().collect(Collectors.toList());
        List<Long> adminIds = records.stream()
                .filter(a -> a.getAdminId() != null)
                .map(NoteApproval::getAdminId).distinct().collect(Collectors.toList());
        
        Map<Long, Note> noteMap = noteService.listByIds(noteIds).stream()
                .collect(Collectors.toMap(Note::getId, n -> n));
        
        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        
        Map<Long, User> adminMap = adminIds.isEmpty() ? Map.of() :
                userService.listByIds(adminIds).stream().collect(Collectors.toMap(User::getId, u -> u));
        
        List<NoteApprovalVO> voList = records.stream().map(approval -> {
            NoteApprovalVO vo = new NoteApprovalVO();
            BeanUtils.copyProperties(approval, vo);
            
            Note note = noteMap.get(approval.getNoteId());
            if (note != null) {
                vo.setNoteTitle(note.getTitle());
            }
            
            User user = userMap.get(approval.getUserId());
            if (user != null) {
                vo.setUsername(user.getNickname() != null ? user.getNickname() : user.getUsername());
            }
            
            if (approval.getAdminId() != null) {
                User admin = adminMap.get(approval.getAdminId());
                if (admin != null) {
                    vo.setAdminName(admin.getNickname() != null ? admin.getNickname() : admin.getUsername());
                }
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }
}
