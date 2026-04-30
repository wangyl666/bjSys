package com.wyl.note.controller;

import com.wyl.note.common.Result;
import com.wyl.note.dto.DraftDTO;
import com.wyl.note.entity.Draft;
import com.wyl.note.security.UserDetailsImpl;
import com.wyl.note.service.DraftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "草稿接口")
@RestController
@RequestMapping("/api/drafts")
@RequiredArgsConstructor
public class DraftController {

    private final DraftService draftService;

    @Operation(summary = "保存草稿")
    @PostMapping
    public Result<Void> saveDraft(@RequestBody DraftDTO draftDTO,
                                   @AuthenticationPrincipal UserDetailsImpl userDetails) {
        draftService.saveDraft(userDetails.getId(), draftDTO);
        return Result.success();
    }

    @Operation(summary = "获取草稿")
    @GetMapping
    public Result<Draft> getDraft(@RequestParam(required = false) Long noteId,
                                   @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Draft draft = draftService.getDraft(userDetails.getId(), noteId);
        return Result.success(draft);
    }

    @Operation(summary = "删除草稿")
    @DeleteMapping
    public Result<Void> deleteDraft(@RequestParam(required = false) Long noteId,
                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {
        draftService.deleteDraft(userDetails.getId(), noteId);
        return Result.success();
    }
}
