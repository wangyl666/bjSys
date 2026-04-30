package com.wyl.note.controller;

import com.wyl.note.common.Result;
import com.wyl.note.security.UserDetailsImpl;
import com.wyl.note.service.NoteFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "收藏接口")
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class NoteFavoriteController {

    private final NoteFavoriteService noteFavoriteService;

    @Operation(summary = "切换收藏状态（收藏/取消收藏）")
    @PostMapping("/toggle")
    public Result<Boolean> toggleFavorite(
            @RequestParam Long noteId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Boolean isFavorited = noteFavoriteService.toggleFavorite(noteId, userDetails.getId());
        return Result.success(isFavorited ? "收藏成功" : "取消收藏成功", isFavorited);
    }

    @Operation(summary = "检查是否已收藏")
    @GetMapping("/check")
    public Result<Boolean> checkFavorite(
            @RequestParam Long noteId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Boolean isFavorited = noteFavoriteService.isFavorited(noteId, userDetails.getId());
        return Result.success(isFavorited);
    }

    @Operation(summary = "获取收藏数")
    @GetMapping("/count")
    public Result<Integer> getFavoriteCount(@RequestParam Long noteId) {
        Integer count = noteFavoriteService.getFavoriteCount(noteId);
        return Result.success(count);
    }
}
