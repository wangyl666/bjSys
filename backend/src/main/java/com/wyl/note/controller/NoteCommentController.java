package com.wyl.note.controller;

import com.wyl.note.common.Result;
import com.wyl.note.dto.CreateCommentDTO;
import com.wyl.note.security.UserDetailsImpl;
import com.wyl.note.service.NoteCommentService;
import com.wyl.note.vo.NoteCommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "评论接口")
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class NoteCommentController {

    private final NoteCommentService noteCommentService;

    @Operation(summary = "创建评论")
    @PostMapping
    public Result<NoteCommentVO> createComment(
            @Valid @RequestBody CreateCommentDTO dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        NoteCommentVO commentVO = noteCommentService.createComment(dto, userDetails.getId());
        return Result.success("评论成功", commentVO);
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        noteCommentService.deleteComment(id, userDetails.getId());
        return Result.success();
    }

    @Operation(summary = "获取笔记的评论列表")
    @GetMapping("/note/{noteId}")
    public Result<List<NoteCommentVO>> getCommentsByNoteId(@PathVariable Long noteId) {
        List<NoteCommentVO> comments = noteCommentService.getCommentsByNoteId(noteId);
        return Result.success(comments);
    }

    @Operation(summary = "获取评论数")
    @GetMapping("/count")
    public Result<Integer> getCommentCount(@RequestParam Long noteId) {
        Integer count = noteCommentService.getCommentCount(noteId);
        return Result.success(count);
    }
}
