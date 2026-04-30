package com.wyl.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyl.note.dto.CreateCommentDTO;
import com.wyl.note.entity.Note;
import com.wyl.note.entity.NoteComment;
import com.wyl.note.entity.User;
import com.wyl.note.mapper.NoteCommentMapper;
import com.wyl.note.service.NoteCommentService;
import com.wyl.note.service.NoteService;
import com.wyl.note.service.UserService;
import com.wyl.note.vo.NoteCommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteCommentServiceImpl extends ServiceImpl<NoteCommentMapper, NoteComment> implements NoteCommentService {

    private final NoteService noteService;
    private final UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoteCommentVO createComment(CreateCommentDTO dto, Long userId) {
        Note note = noteService.getById(dto.getNoteId());
        if (note == null) {
            throw new RuntimeException("笔记不存在");
        }
        if (note.getIsPublic() == 0) {
            throw new RuntimeException("该笔记未公开，无法评论");
        }
        if (!"APPROVED".equals(note.getApprovalStatus())) {
            throw new RuntimeException("该笔记尚未通过审核，无法评论");
        }

        if (dto.getParentId() != null) {
            NoteComment parentComment = getById(dto.getParentId());
            if (parentComment == null) {
                throw new RuntimeException("父评论不存在");
            }
            if (!parentComment.getNoteId().equals(dto.getNoteId())) {
                throw new RuntimeException("父评论不属于该笔记");
            }
        }

        NoteComment comment = new NoteComment();
        comment.setNoteId(dto.getNoteId());
        comment.setUserId(userId);
        comment.setParentId(dto.getParentId());
        comment.setReplyToUserId(dto.getReplyToUserId());
        comment.setContent(dto.getContent());
        comment.setStatus(1);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        save(comment);

        return convertToVO(comment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, Long userId) {
        NoteComment comment = getById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除此评论");
        }
        removeById(commentId);
    }

    @Override
    public List<NoteCommentVO> getCommentsByNoteId(Long noteId) {
        List<NoteComment> comments = list(
                new LambdaQueryWrapper<NoteComment>()
                        .eq(NoteComment::getNoteId, noteId)
                        .eq(NoteComment::getStatus, 1)
                        .orderByAsc(NoteComment::getCreatedAt)
        );

        if (CollectionUtils.isEmpty(comments)) {
            return new ArrayList<>();
        }

        List<Long> userIds = comments.stream().map(NoteComment::getUserId).distinct().collect(Collectors.toList());
        List<Long> replyUserIds = comments.stream()
                .filter(c -> c.getReplyToUserId() != null)
                .map(NoteComment::getReplyToUserId)
                .distinct()
                .collect(Collectors.toList());
        userIds.addAll(replyUserIds);
        userIds = userIds.stream().distinct().collect(Collectors.toList());

        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<NoteCommentVO> allVOs = comments.stream()
                .map(comment -> convertToVOWithUsers(comment, userMap))
                .collect(Collectors.toList());

        return buildCommentTree(allVOs);
    }

    @Override
    public Integer getCommentCount(Long noteId) {
        return count(
                new LambdaQueryWrapper<NoteComment>()
                        .eq(NoteComment::getNoteId, noteId)
                        .eq(NoteComment::getStatus, 1)
        );
    }

    private NoteCommentVO convertToVO(NoteComment comment) {
        NoteCommentVO vo = new NoteCommentVO();
        BeanUtils.copyProperties(comment, vo);

        User user = userService.getById(comment.getUserId());
        if (user != null) {
            vo.setUsername(user.getNickname() != null ? user.getNickname() : user.getUsername());
            vo.setUserAvatar(user.getAvatar());
        }

        if (comment.getReplyToUserId() != null) {
            User replyUser = userService.getById(comment.getReplyToUserId());
            if (replyUser != null) {
                vo.setReplyToUsername(replyUser.getNickname() != null ? replyUser.getNickname() : replyUser.getUsername());
            }
        }

        return vo;
    }

    private NoteCommentVO convertToVOWithUsers(NoteComment comment, Map<Long, User> userMap) {
        NoteCommentVO vo = new NoteCommentVO();
        BeanUtils.copyProperties(comment, vo);

        User user = userMap.get(comment.getUserId());
        if (user != null) {
            vo.setUsername(user.getNickname() != null ? user.getNickname() : user.getUsername());
            vo.setUserAvatar(user.getAvatar());
        }

        if (comment.getReplyToUserId() != null) {
            User replyUser = userMap.get(comment.getReplyToUserId());
            if (replyUser != null) {
                vo.setReplyToUsername(replyUser.getNickname() != null ? replyUser.getNickname() : replyUser.getUsername());
            }
        }

        return vo;
    }

    private List<NoteCommentVO> buildCommentTree(List<NoteCommentVO> allVOs) {
        Map<Long, NoteCommentVO> voMap = allVOs.stream()
                .collect(Collectors.toMap(NoteCommentVO::getId, vo -> vo));

        List<NoteCommentVO> roots = new ArrayList<>();

        for (NoteCommentVO vo : allVOs) {
            if (vo.getParentId() == null) {
                roots.add(vo);
            } else {
                NoteCommentVO parent = voMap.get(vo.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(vo);
                }
            }
        }

        return roots;
    }
}
