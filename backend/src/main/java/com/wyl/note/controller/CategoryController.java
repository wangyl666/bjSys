package com.wyl.note.controller;

import com.wyl.note.common.Result;
import com.wyl.note.security.UserDetailsImpl;
import com.wyl.note.service.CategoryService;
import com.wyl.note.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "分类接口")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "创建分类")
    @PostMapping
    public Result<CategoryVO> createCategory(@RequestParam String name,
                                              @AuthenticationPrincipal UserDetailsImpl userDetails) {
        CategoryVO categoryVO = categoryService.createCategory(name, userDetails.getId());
        return Result.success("创建成功", categoryVO);
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        categoryService.deleteCategory(id, userDetails.getId());
        return Result.success();
    }

    @Operation(summary = "获取用户分类列表")
    @GetMapping
    public Result<List<CategoryVO>> getUserCategories(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<CategoryVO> categories = categoryService.getUserCategories(userDetails.getId());
        return Result.success(categories);
    }
}
