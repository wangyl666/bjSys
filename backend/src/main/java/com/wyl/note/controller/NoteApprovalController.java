package com.wyl.note.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wyl.note.common.Result;
import com.wyl.note.dto.ProcessApprovalDTO;
import com.wyl.note.dto.SubmitApprovalDTO;
import com.wyl.note.security.UserDetailsImpl;
import com.wyl.note.service.NoteApprovalService;
import com.wyl.note.vo.NoteApprovalVO;
import com.wyl.note.vo.NoteVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "审批接口")
@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class NoteApprovalController {

    private final NoteApprovalService noteApprovalService;

    @Operation(summary = "提交审批")
    @PostMapping("/submit")
    public Result<NoteApprovalVO> submitApproval(@RequestBody SubmitApprovalDTO dto,
                                                   @AuthenticationPrincipal UserDetailsImpl userDetails) {
        NoteApprovalVO approvalVO = noteApprovalService.submitApproval(dto.getNoteId(), userDetails.getId());
        return Result.success("提交审批成功", approvalVO);
    }

    @Operation(summary = "获取我的审批记录")
    @GetMapping("/my")
    public Result<Page<NoteApprovalVO>> getMyApprovals(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Page<NoteApprovalVO> approvalPage = noteApprovalService.getMyApprovals(userDetails.getId(), page, size);
        return Result.success(approvalPage);
    }

    @Operation(summary = "获取审批详情")
    @GetMapping("/{id}")
    public Result<NoteApprovalVO> getApprovalDetail(@PathVariable Long id,
                                                       @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("USER");
        NoteApprovalVO approvalVO = noteApprovalService.getApprovalDetail(id, userDetails.getId(), role);
        return Result.success(approvalVO);
    }

    @Operation(summary = "管理员获取待审批列表")
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<NoteApprovalVO>> getPendingApprovals(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<NoteApprovalVO> approvalPage = noteApprovalService.getPendingApprovals(page, size);
        return Result.success(approvalPage);
    }

    @Operation(summary = "管理员处理审批")
    @PostMapping("/process")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<NoteApprovalVO> processApproval(@RequestBody ProcessApprovalDTO dto,
                                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {
        NoteApprovalVO approvalVO = noteApprovalService.processApproval(
                dto.getApprovalId(), userDetails.getId(), dto.getApproved(), dto.getRemark());
        return Result.success(dto.getApproved() ? "审批通过" : "审批拒绝", approvalVO);
    }

    @Operation(summary = "管理员查看审批记录（标记为已查看）")
    @PostMapping("/view/{approvalId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<NoteApprovalVO> viewApproval(@PathVariable Long approvalId,
                                                 @AuthenticationPrincipal UserDetailsImpl userDetails) {
        NoteApprovalVO approvalVO = noteApprovalService.viewApproval(approvalId, userDetails.getId());
        return Result.success("已标记为已查看", approvalVO);
    }

    @Operation(summary = "管理员查看待审核笔记详情（同时标记为已查看）")
    @GetMapping("/view-note/{approvalId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<NoteVO> viewApprovalNote(@PathVariable Long approvalId,
                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        NoteVO noteVO = noteApprovalService.viewApprovalNote(approvalId, userDetails.getId());
        return Result.success(noteVO);
    }
}
