package com.wyl.note.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wyl.note.dto.DraftDTO;
import com.wyl.note.entity.Draft;
import com.wyl.note.service.DraftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DraftServiceImpl implements DraftService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String DRAFT_PREFIX = "draft:";
    private static final long DRAFT_EXPIRE_DAYS = 7;

    @Override
    public void saveDraft(Long userId, DraftDTO draftDTO) {
        try {
            Draft draft = new Draft();
            draft.setUserId(userId);
            draft.setNoteId(draftDTO.getNoteId());
            draft.setTitle(draftDTO.getTitle());
            draft.setContent(draftDTO.getContent());
            draft.setCategoryId(draftDTO.getCategoryId());
            draft.setTagIds(draftDTO.getTagIds());
            draft.setSavedAt(LocalDateTime.now());

            String key = getDraftKey(userId, draftDTO.getNoteId());
            redisTemplate.opsForValue().set(key, draft, DRAFT_EXPIRE_DAYS, TimeUnit.DAYS);
            log.info("保存草稿成功: userId={}, noteId={}", userId, draftDTO.getNoteId());
        } catch (Exception e) {
            log.error("保存草稿失败", e);
            throw new RuntimeException("保存草稿失败");
        }
    }

    @Override
    public Draft getDraft(Long userId, Long noteId) {
        try {
            String key = getDraftKey(userId, noteId);
            Object obj = redisTemplate.opsForValue().get(key);
            if (obj == null) {
                return null;
            }
            return objectMapper.convertValue(obj, Draft.class);
        } catch (Exception e) {
            log.error("获取草稿失败", e);
            return null;
        }
    }

    @Override
    public void deleteDraft(Long userId, Long noteId) {
        try {
            String key = getDraftKey(userId, noteId);
            redisTemplate.delete(key);
            log.info("删除草稿成功: userId={}, noteId={}", userId, noteId);
        } catch (Exception e) {
            log.error("删除草稿失败", e);
        }
    }

    private String getDraftKey(Long userId, Long noteId) {
        return DRAFT_PREFIX + userId + ":" + (noteId != null ? noteId : "new");
    }
}
