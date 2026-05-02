package com.wyl.note.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wyl.note.common.Result;
import com.wyl.note.dto.NoteDTO;
import com.wyl.note.security.UserDetailsImpl;
import com.wyl.note.service.NoteService;
import com.wyl.note.vo.NoteVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "笔记接口")
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @Operation(summary = "创建笔记")
    @PostMapping
    public Result<NoteVO> createNote(@Valid @RequestBody NoteDTO noteDTO,
                                      @AuthenticationPrincipal UserDetailsImpl userDetails) {
        NoteVO noteVO = noteService.createNote(noteDTO, userDetails.getId());
        return Result.success("创建成功", noteVO);
    }

    @Operation(summary = "更新笔记")
    @PutMapping
    public Result<NoteVO> updateNote(@Valid @RequestBody NoteDTO noteDTO,
                                      @AuthenticationPrincipal UserDetailsImpl userDetails) {
        NoteVO noteVO = noteService.updateNote(noteDTO, userDetails.getId());
        return Result.success("更新成功", noteVO);
    }

    @Operation(summary = "删除笔记")
    @DeleteMapping("/{id}")
    public Result<Void> deleteNote(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {
        noteService.deleteNote(id, userDetails.getId());
        return Result.success();
    }

    @Operation(summary = "获取笔记详情")
    @GetMapping("/{id}")
    public Result<NoteVO> getNoteById(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetailsImpl userDetails) {
        NoteVO noteVO = noteService.getNoteById(id, userDetails.getId());
        return Result.success(noteVO);
    }

    @Operation(summary = "分页获取笔记列表")
    @GetMapping
    public Result<Page<NoteVO>> getNotePage(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long tagId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Page<NoteVO> notePage = noteService.getNotePage(categoryId, keyword, tagId, page, size, userDetails.getId());
        return Result.success(notePage);
    }

    @Operation(summary = "复制笔记")
    @PostMapping("/{id}/copy")
    public Result<NoteVO> copyNote(@PathVariable Long id,
                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {
        NoteVO noteVO = noteService.copyNote(id, userDetails.getId());
        return Result.success("复制成功", noteVO);
    }
}
