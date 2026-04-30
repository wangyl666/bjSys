package com.wyl.note.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wyl.note.vo.CommunityNoteVO;

public interface CommunityNoteService {

    Page<CommunityNoteVO> getCommunityNotes(Integer page, Integer size, String keyword, Long categoryId, Long userId);

    CommunityNoteVO getCommunityNoteDetail(Long noteId, Long userId);

    void incrementViewCount(Long noteId);
}
