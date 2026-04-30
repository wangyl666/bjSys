package com.wyl.note.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wyl.note.dto.NoteDTO;
import com.wyl.note.entity.Note;
import com.wyl.note.vo.NoteVO;

public interface NoteService extends IService<Note> {

    NoteVO createNote(NoteDTO noteDTO, Long userId);

    NoteVO updateNote(NoteDTO noteDTO, Long userId);

    void deleteNote(Long id, Long userId);

    NoteVO getNoteById(Long id, Long userId);

    Page<NoteVO> getNotePage(Long categoryId, String keyword, Integer page, Integer size, Long userId);
}
