package com.wyl.note.controller;

import com.wyl.note.common.Result;
import com.wyl.note.security.UserDetailsImpl;
import com.wyl.note.service.UserService;
import com.wyl.note.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户接口")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/current")
    public Result<UserVO> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return Result.error(401, "未登录");
        }
        UserVO userVO = userService.getCurrentUser(userDetails.getId());
        return Result.success(userVO);
    }
}
