package com.wyl.note.controller;

import com.wyl.note.common.Result;
import com.wyl.note.security.UserDetailsImpl;
import com.wyl.note.service.TagService;
import com.wyl.note.vo.TagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "标签接口")
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @Operation(summary = "创建标签")
    @PostMapping
    public Result<TagVO> createTag(@RequestParam String name,
                                    @RequestParam(required = false) String color,
                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {
        TagVO tagVO = tagService.createTag(name, color, userDetails.getId());
        return Result.success("创建成功", tagVO);
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetailsImpl userDetails) {
        tagService.deleteTag(id, userDetails.getId());
        return Result.success();
    }

    @Operation(summary = "获取用户标签列表")
    @GetMapping
    public Result<List<TagVO>> getUserTags(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<TagVO> tags = tagService.getUserTags(userDetails.getId());
        return Result.success(tags);
    }
}
