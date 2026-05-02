package com.wyl.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyl.note.dto.NoteDTO;
import com.wyl.note.entity.Category;
import com.wyl.note.entity.Note;
import com.wyl.note.entity.NoteTag;
import com.wyl.note.entity.Tag;
import com.wyl.note.mapper.NoteMapper;
import com.wyl.note.service.*;
import com.wyl.note.vo.NoteVO;
import com.wyl.note.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {

    private final NoteTagService noteTagService;
    private final TagService tagService;
    private final CategoryService categoryService;
    private final DraftService draftService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoteVO createNote(NoteDTO noteDTO, Long userId) {
        Note note = new Note();
        BeanUtils.copyProperties(noteDTO, note);
        note.setUserId(userId);
        note.setViewCount(0);
        note.setIsPublic(noteDTO.getIsPublic() != null ? noteDTO.getIsPublic() : 0);
        note.setApprovalStatus("DRAFT");
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());
        save(note);

        saveNoteTags(note.getId(), noteDTO.getTagIds());

        draftService.deleteDraft(userId, note.getId());

        return getNoteById(note.getId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoteVO updateNote(NoteDTO noteDTO, Long userId) {
        Note note = getById(noteDTO.getId());
        if (note == null || !note.getUserId().equals(userId)) {
            throw new RuntimeException("笔记不存在或无权限操作");
        }
        
        boolean wasApproved = "APPROVED".equals(note.getApprovalStatus());
        boolean isPublic = noteDTO.getIsPublic() != null && noteDTO.getIsPublic() == 1;
        
        BeanUtils.copyProperties(noteDTO, note);
        note.setUpdatedAt(LocalDateTime.now());
        
        if (wasApproved && !isPublic) {
            note.setApprovalStatus("DRAFT");
        } else if (wasApproved && isPublic) {
            note.setApprovalStatus("DRAFT");
        }
        
        updateById(note);

        noteTagService.remove(new LambdaQueryWrapper<NoteTag>().eq(NoteTag::getNoteId, note.getId()));
        saveNoteTags(note.getId(), noteDTO.getTagIds());

        draftService.deleteDraft(userId, note.getId());

        return getNoteById(note.getId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNote(Long id, Long userId) {
        Note note = getById(id);
        if (note == null || !note.getUserId().equals(userId)) {
            throw new RuntimeException("笔记不存在或无权限操作");
        }
        removeById(id);
        noteTagService.remove(new LambdaQueryWrapper<NoteTag>().eq(NoteTag::getNoteId, id));
    }

    @Override
    public NoteVO getNoteById(Long id, Long userId) {
        Note note = getById(id);
        if (note == null) {
            throw new RuntimeException("笔记不存在");
        }
        if (!note.getUserId().equals(userId)) {
            if (note.getIsPublic() == 0) {
                throw new RuntimeException("无权限查看此笔记");
            }
            if (!"APPROVED".equals(note.getApprovalStatus())) {
                throw new RuntimeException("该笔记尚未通过审核");
            }
        }
        return convertToNoteVO(note);
    }

    @Override
    public NoteVO getNoteByIdForAdmin(Long id) {
        Note note = getById(id);
        if (note == null) {
            throw new RuntimeException("笔记不存在");
        }
        return convertToNoteVO(note);
    }

    private NoteVO convertToNoteVO(Note note) {
        NoteVO vo = new NoteVO();
        BeanUtils.copyProperties(note, vo);

        if (note.getCategoryId() != null) {
            Category category = categoryService.getById(note.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }

        List<NoteTag> noteTags = noteTagService.list(
                new LambdaQueryWrapper<NoteTag>().eq(NoteTag::getNoteId, note.getId()));
        if (!CollectionUtils.isEmpty(noteTags)) {
            List<Long> tagIds = noteTags.stream().map(NoteTag::getTagId).collect(Collectors.toList());
            List<Tag> tags = tagService.listByIds(tagIds);
            List<TagVO> tagVOS = tags.stream().map(tag -> {
                TagVO tagVO = new TagVO();
                BeanUtils.copyProperties(tag, tagVO);
                return tagVO;
            }).collect(Collectors.toList());
            vo.setTags(tagVOS);
        }

        return vo;
    }

    @Override
    public Page<NoteVO> getNotePage(Long categoryId, String keyword, Long tagId, Integer page, Integer size, Long userId) {
        Page<Note> notePage = new Page<>(page, size);
        Page<Note> resultPage;

        if (tagId != null) {
            if (categoryId != null && StringUtils.hasText(keyword)) {
                resultPage = baseMapper.selectNotesByTagIdCategoryIdAndKeyword(notePage, tagId, categoryId, keyword, userId);
            } else if (categoryId != null) {
                resultPage = baseMapper.selectNotesByTagIdAndCategoryId(notePage, tagId, categoryId, userId);
            } else if (StringUtils.hasText(keyword)) {
                resultPage = baseMapper.selectNotesByTagIdAndKeyword(notePage, tagId, keyword, userId);
            } else {
                resultPage = baseMapper.selectNotesByTagId(notePage, tagId, userId);
            }
        } else {
            LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Note::getUserId, userId)
                    .orderByDesc(Note::getUpdatedAt);

            if (categoryId != null) {
                wrapper.eq(Note::getCategoryId, categoryId);
            }
            if (StringUtils.hasText(keyword)) {
                wrapper.and(w -> w.like(Note::getTitle, keyword)
                        .or().like(Note::getContent, keyword)
                        .or().like(Note::getSummary, keyword));
            }

            resultPage = page(notePage, wrapper);
        }

        Page<NoteVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());

        List<NoteVO> voList = new ArrayList<>();
        for (Note note : resultPage.getRecords()) {
            NoteVO vo = new NoteVO();
            BeanUtils.copyProperties(note, vo);

            if (note.getCategoryId() != null) {
                Category category = categoryService.getById(note.getCategoryId());
                if (category != null) {
                    vo.setCategoryName(category.getName());
                }
            }
            voList.add(vo);
        }
        voPage.setRecords(voList);

        return voPage;
    }

    private void saveNoteTags(Long noteId, List<Long> tagIds) {
        if (!CollectionUtils.isEmpty(tagIds)) {
            List<NoteTag> noteTags = tagIds.stream().map(tagId -> {
                NoteTag noteTag = new NoteTag();
                noteTag.setNoteId(noteId);
                noteTag.setTagId(tagId);
                return noteTag;
            }).collect(Collectors.toList());
            noteTagService.saveBatch(noteTags);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoteVO copyNote(Long id, Long userId) {
        Note originalNote = getById(id);
        if (originalNote == null) {
            throw new RuntimeException("笔记不存在");
        }
        if (!originalNote.getUserId().equals(userId) && originalNote.getIsPublic() == 0) {
            throw new RuntimeException("无权限复制此笔记");
        }
        if (!originalNote.getUserId().equals(userId) && !"APPROVED".equals(originalNote.getApprovalStatus())) {
            throw new RuntimeException("该笔记尚未通过审核，无法复制");
        }

        Note copiedNote = new Note();
        BeanUtils.copyProperties(originalNote, copiedNote);
        copiedNote.setId(null);
        copiedNote.setUserId(userId);
        copiedNote.setTitle(originalNote.getTitle() + " (副本)");
        copiedNote.setViewCount(0);
        copiedNote.setIsPublic(0);
        copiedNote.setApprovalStatus("DRAFT");
        copiedNote.setCreatedAt(LocalDateTime.now());
        copiedNote.setUpdatedAt(LocalDateTime.now());
        save(copiedNote);

        List<NoteTag> originalNoteTags = noteTagService.list(
                new LambdaQueryWrapper<NoteTag>().eq(NoteTag::getNoteId, id));
        if (!CollectionUtils.isEmpty(originalNoteTags)) {
            List<Long> tagIds = originalNoteTags.stream()
                    .map(NoteTag::getTagId)
                    .collect(Collectors.toList());
            saveNoteTags(copiedNote.getId(), tagIds);
        }

        return getNoteById(copiedNote.getId(), userId);
    }
}
