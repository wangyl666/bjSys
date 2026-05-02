package com.wyl.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyl.note.entity.ChatMessage;
import com.wyl.note.entity.User;
import com.wyl.note.mapper.ChatMessageMapper;
import com.wyl.note.service.ChatMessageService;
import com.wyl.note.service.UserService;
import com.wyl.note.vo.ChatMessageVO;
import com.wyl.note.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    private final UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessageVO saveMessage(Long userId, String content, String messageType) {
        ChatMessage message = new ChatMessage();
        message.setUserId(userId);
        message.setContent(content);
        message.setMessageType(messageType != null ? messageType : "TEXT");
        message.setCreatedAt(LocalDateTime.now());
        save(message);
        
        return convertToVO(message, userId);
    }

    @Override
    public Page<ChatMessageVO> getMessagePage(int page, int size) {
        Page<ChatMessage> messagePage = new Page<>(page, size);
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ChatMessage::getCreatedAt);
        messagePage = page(messagePage, wrapper);
        
        Page<ChatMessageVO> voPage = new Page<>();
        BeanUtils.copyProperties(messagePage, voPage);
        
        List<ChatMessage> records = messagePage.getRecords();
        if (!records.isEmpty()) {
            Set<Long> userIds = records.stream()
                    .map(ChatMessage::getUserId)
                    .collect(Collectors.toSet());
            
            List<User> users = userService.listByIds(userIds);
            Map<Long, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
            
            List<ChatMessageVO> voRecords = records.stream()
                    .map(msg -> convertToVO(msg, userMap))
                    .collect(Collectors.toList());
            
            voPage.setRecords(voRecords);
        }
        
        return voPage;
    }

    private ChatMessageVO convertToVO(ChatMessage message, Long userId) {
        ChatMessageVO vo = new ChatMessageVO();
        BeanUtils.copyProperties(message, vo);
        
        User user = userService.getById(userId);
        if (user != null) {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            vo.setUser(userVO);
        }
        
        return vo;
    }

    private ChatMessageVO convertToVO(ChatMessage message, Map<Long, User> userMap) {
        ChatMessageVO vo = new ChatMessageVO();
        BeanUtils.copyProperties(message, vo);
        
        User user = userMap.get(message.getUserId());
        if (user != null) {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            vo.setUser(userVO);
        }
        
        return vo;
    }
}
