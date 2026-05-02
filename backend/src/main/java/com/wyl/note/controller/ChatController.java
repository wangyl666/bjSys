package com.wyl.note.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wyl.note.common.Result;
import com.wyl.note.service.ChatMessageService;
import com.wyl.note.vo.ChatMessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "聊天室接口")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService chatMessageService;

    @Operation(summary = "获取历史消息列表")
    @GetMapping("/messages")
    public Result<Page<ChatMessageVO>> getMessages(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<ChatMessageVO> messages = chatMessageService.getMessagePage(page, size);
        return Result.success(messages);
    }
}
