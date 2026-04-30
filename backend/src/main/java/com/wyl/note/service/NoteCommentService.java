package com.wyl.note.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wyl.note.dto.CreateCommentDTO;
import com.wyl.note.entity.NoteComment;
import com.wyl.note.vo.NoteCommentVO;

import java.util.List;

public interface NoteCommentService extends IService<NoteComment> {

    NoteCommentVO createComment(CreateCommentDTO dto, Long userId);

    void deleteComment(Long commentId, Long userId);

    List<NoteCommentVO> getCommentsByNoteId(Long noteId);

    Long getCommentCount(Long noteId);
}
