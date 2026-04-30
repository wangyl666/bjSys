package com.wyl.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wyl.note.entity.*;
import com.wyl.note.service.*;
import com.wyl.note.vo.CommunityNoteVO;
import com.wyl.note.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityNoteServiceImpl implements CommunityNoteService {

    private final NoteService noteService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final NoteTagService noteTagService;
    private final TagService tagService;
    private final NoteFavoriteService noteFavoriteService;
    private final NoteCommentService noteCommentService;

    @Override
    public Page<CommunityNoteVO> getCommunityNotes(Integer page, Integer size, String keyword, Long categoryId, Long userId) {
        Page<Note> notePage = new Page<>(page, size);
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getIsPublic, 1)
                .eq(Note::getApprovalStatus, "APPROVED")
                .orderByDesc(Note::getCreatedAt);

        if (categoryId != null) {
            wrapper.eq(Note::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Note::getTitle, keyword)
                    .or().like(Note::getContent, keyword)
                    .or().like(Note::getSummary, keyword));
        }

        Page<Note> resultPage = noteService.page(notePage, wrapper);
        Page<CommunityNoteVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());

        List<Note> notes = resultPage.getRecords();
        if (CollectionUtils.isEmpty(notes)) {
            voPage.setRecords(new ArrayList<>());
            return voPage;
        }

        List<Long> userIds = notes.stream().map(Note::getUserId).distinct().collect(Collectors.toList());
        List<Long> noteIds = notes.stream().map(Note::getId).distinct().collect(Collectors.toList());
        List<Long> categoryIds = notes.stream()
                .filter(n -> n.getCategoryId() != null)
                .map(Note::getCategoryId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        Map<Long, Category> categoryMap = categoryIds.isEmpty() ? Collections.emptyMap() :
                categoryService.listByIds(categoryIds).stream()
                        .collect(Collectors.toMap(Category::getId, c -> c));

        Map<Long, List<TagVO>> noteTagsMap = getNoteTagsMap(noteIds);

        Map<Long, Long> favoriteCountMap = noteIds.stream()
                .collect(Collectors.toMap(noteId -> noteId, noteFavoriteService::getFavoriteCount));

        Map<Long, Long> commentCountMap = noteIds.stream()
                .collect(Collectors.toMap(noteId -> noteId, noteCommentService::getCommentCount));

        Map<Long, Boolean> isFavoritedMap = userId == null ? Collections.emptyMap() :
                noteIds.stream()
                        .collect(Collectors.toMap(noteId -> noteId, noteId -> noteFavoriteService.isFavorited(noteId, userId)));

        List<CommunityNoteVO> voList = notes.stream().map(note -> {
            CommunityNoteVO vo = new CommunityNoteVO();
            BeanUtils.copyProperties(note, vo);

            User user = userMap.get(note.getUserId());
            if (user != null) {
                vo.setUsername(user.getNickname() != null ? user.getNickname() : user.getUsername());
                vo.setUserAvatar(user.getAvatar());
            }

            if (note.getCategoryId() != null) {
                Category category = categoryMap.get(note.getCategoryId());
                if (category != null) {
                    vo.setCategoryName(category.getName());
                }
            }

            vo.setTags(noteTagsMap.getOrDefault(note.getId(), new ArrayList<>()));
            vo.setFavoriteCount(favoriteCountMap.getOrDefault(note.getId(), 0L));
            vo.setCommentCount(commentCountMap.getOrDefault(note.getId(), 0L));
            vo.setIsFavorited(isFavoritedMap.getOrDefault(note.getId(), false));

            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommunityNoteVO getCommunityNoteDetail(Long noteId, Long userId) {
        Note note = noteService.getById(noteId);
        if (note == null) {
            throw new RuntimeException("笔记不存在");
        }
        if (note.getIsPublic() == 0) {
            throw new RuntimeException("该笔记未公开");
        }
        if (!"APPROVED".equals(note.getApprovalStatus())) {
            throw new RuntimeException("该笔记尚未通过审核");
        }

        CommunityNoteVO vo = new CommunityNoteVO();
        BeanUtils.copyProperties(note, vo);

        User user = userService.getById(note.getUserId());
        if (user != null) {
            vo.setUsername(user.getNickname() != null ? user.getNickname() : user.getUsername());
            vo.setUserAvatar(user.getAvatar());
        }

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
        } else {
            vo.setTags(new ArrayList<>());
        }

        vo.setFavoriteCount(noteFavoriteService.getFavoriteCount(noteId));
        vo.setCommentCount(noteCommentService.getCommentCount(noteId));
        vo.setIsFavorited(userId != null && noteFavoriteService.isFavorited(noteId, userId));

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementViewCount(Long noteId) {
        Note note = noteService.getById(noteId);
        if (note != null) {
            note.setViewCount(note.getViewCount() + 1);
            noteService.updateById(note);
        }
    }

    private Map<Long, List<TagVO>> getNoteTagsMap(List<Long> noteIds) {
        if (CollectionUtils.isEmpty(noteIds)) {
            return Collections.emptyMap();
        }

        List<NoteTag> noteTags = noteTagService.list(
                new LambdaQueryWrapper<NoteTag>().in(NoteTag::getNoteId, noteIds));

        if (CollectionUtils.isEmpty(noteTags)) {
            return Collections.emptyMap();
        }

        List<Long> tagIds = noteTags.stream().map(NoteTag::getTagId).distinct().collect(Collectors.toList());
        Map<Long, Tag> tagMap = tagService.listByIds(tagIds).stream()
                .collect(Collectors.toMap(Tag::getId, t -> t));

        Map<Long, List<TagVO>> result = noteTags.stream()
                .collect(Collectors.groupingBy(
                        NoteTag::getNoteId,
                        Collectors.mapping(nt -> {
                            TagVO tagVO = new TagVO();
                            Tag tag = tagMap.get(nt.getTagId());
                            if (tag != null) {
                                BeanUtils.copyProperties(tag, tagVO);
                            }
                            return tagVO;
                        }, Collectors.toList())
                ));

        return result;
    }
}
