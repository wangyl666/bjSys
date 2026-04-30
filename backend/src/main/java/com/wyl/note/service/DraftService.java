package com.wyl.note.service;

import com.wyl.note.dto.DraftDTO;
import com.wyl.note.entity.Draft;

public interface DraftService {

    void saveDraft(Long userId, DraftDTO draftDTO);

    Draft getDraft(Long userId, Long noteId);

    void deleteDraft(Long userId, Long noteId);
}
