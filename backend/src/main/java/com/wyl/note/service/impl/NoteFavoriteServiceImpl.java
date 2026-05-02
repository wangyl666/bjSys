package com.wyl.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyl.note.entity.Note;
import com.wyl.note.entity.NoteFavorite;
import com.wyl.note.mapper.NoteFavoriteMapper;
import com.wyl.note.service.NoteFavoriteService;
import com.wyl.note.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NoteFavoriteServiceImpl extends ServiceImpl<NoteFavoriteMapper, NoteFavorite> implements NoteFavoriteService {

    private final NoteService noteService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean toggleFavorite(Long noteId, Long userId) {
        Note note = noteService.getById(noteId);
        if (note == null) {
            throw new RuntimeException("笔记不存在");
        }
        if (note.getIsPublic() == 0) {
            throw new RuntimeException("该笔记未公开，无法收藏");
        }
        if (!"APPROVED".equals(note.getApprovalStatus())) {
            throw new RuntimeException("该笔记尚未通过审核，无法收藏");
        }

        NoteFavorite existing = getOne(
                new LambdaQueryWrapper<NoteFavorite>()
                        .eq(NoteFavorite::getNoteId, noteId)
                        .eq(NoteFavorite::getUserId, userId)
        );

        if (existing != null) {
            removeById(existing.getId());
            return false;
        } else {
            NoteFavorite favorite = new NoteFavorite();
            favorite.setNoteId(noteId);
            favorite.setUserId(userId);
            favorite.setCreatedAt(LocalDateTime.now());
            save(favorite);
            return true;
        }
    }

    @Override
    public Long getFavoriteCount(Long noteId) {
        return count(
                new LambdaQueryWrapper<NoteFavorite>()
                        .eq(NoteFavorite::getNoteId, noteId)
        );
    }

    @Override
    public Boolean isFavorited(Long noteId, Long userId) {
        NoteFavorite favorite = getOne(
                new LambdaQueryWrapper<NoteFavorite>()
                        .eq(NoteFavorite::getNoteId, noteId)
                        .eq(NoteFavorite::getUserId, userId)
        );
        return favorite != null;
    }
}
