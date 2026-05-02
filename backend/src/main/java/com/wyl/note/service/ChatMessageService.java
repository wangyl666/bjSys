package com.wyl.note.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wyl.note.entity.ChatMessage;
import com.wyl.note.vo.ChatMessageVO;

public interface ChatMessageService extends IService<ChatMessage> {

    ChatMessageVO saveMessage(Long userId, String content, String messageType);

    Page<ChatMessageVO> getMessagePage(int page, int size);
}
