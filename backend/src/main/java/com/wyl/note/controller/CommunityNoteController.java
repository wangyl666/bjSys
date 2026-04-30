package com.wyl.note.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wyl.note.common.Result;
import com.wyl.note.security.UserDetailsImpl;
import com.wyl.note.service.CommunityNoteService;
import com.wyl.note.vo.CommunityNoteVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "社区笔记接口")
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityNoteController {

    private final CommunityNoteService communityNoteService;

    @Operation(summary = "获取社区笔记列表")
    @GetMapping("/notes")
    public Result<Page<CommunityNoteVO>> getCommunityNotes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        Page<CommunityNoteVO> notePage = communityNoteService.getCommunityNotes(page, size, keyword, categoryId, userId);
        return Result.success(notePage);
    }

    @Operation(summary = "获取社区笔记详情")
    @GetMapping("/notes/{id}")
    public Result<CommunityNoteVO> getCommunityNoteDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        communityNoteService.incrementViewCount(id);
        CommunityNoteVO noteVO = communityNoteService.getCommunityNoteDetail(id, userId);
        return Result.success(noteVO);
    }
}
