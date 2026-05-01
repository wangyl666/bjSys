package com.wyl.note.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wyl.note.entity.NoteFavorite;

public interface NoteFavoriteService extends IService<NoteFavorite> {

    Boolean toggleFavorite(Long noteId, Long userId);

    Long getFavoriteCount(Long noteId);

    Boolean isFavorited(Long noteId, Long userId);
}
