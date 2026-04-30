package com.wyl.note.controller;

import com.wyl.note.common.Result;
import com.wyl.note.dto.UpdatePasswordDTO;
import com.wyl.note.dto.UpdateUserInfoDTO;
import com.wyl.note.security.UserDetailsImpl;
import com.wyl.note.service.FileService;
import com.wyl.note.service.UserService;
import com.wyl.note.vo.UserStatsVO;
import com.wyl.note.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@Tag(name = "用户接口")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FileService fileService;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/current")
    public Result<UserVO> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return Result.error(401, "未登录");
        }
        UserVO userVO = userService.getCurrentUser(userDetails.getId());
        return Result.success(userVO);
    }

    @Operation(summary = "获取用户统计数据")
    @GetMapping("/stats")
    public Result<UserStatsVO> getUserStats(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return Result.error(401, "未登录");
        }
        UserStatsVO stats = userService.getUserStats(userDetails.getId());
        return Result.success(stats);
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/info")
    public Result<UserVO> updateUserInfo(@RequestBody @Valid UpdateUserInfoDTO updateUserInfoDTO,
                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return Result.error(401, "未登录");
        }
        UserVO userVO = userService.updateUserInfo(userDetails.getId(), updateUserInfoDTO);
        return Result.success(userVO);
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody @Valid UpdatePasswordDTO updatePasswordDTO,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return Result.error(401, "未登录");
        }
        userService.updatePassword(userDetails.getId(), updatePasswordDTO);
        return Result.success();
    }

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public Result<UserVO> uploadAvatar(@RequestParam("file") MultipartFile file,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return Result.error(401, "未登录");
        }
        String avatarUrl = fileService.uploadFile(file, userDetails.getId());
        UserVO userVO = userService.updateAvatar(userDetails.getId(), avatarUrl);
        return Result.success(userVO);
    }
}
