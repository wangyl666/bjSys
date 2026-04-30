package com.wyl.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyl.note.dto.LoginDTO;
import com.wyl.note.dto.RegisterDTO;
import com.wyl.note.dto.UpdatePasswordDTO;
import com.wyl.note.dto.UpdateUserInfoDTO;
import com.wyl.note.entity.Category;
import com.wyl.note.entity.Note;
import com.wyl.note.entity.Tag;
import com.wyl.note.entity.User;
import com.wyl.note.mapper.UserMapper;
import com.wyl.note.service.CategoryService;
import com.wyl.note.service.NoteService;
import com.wyl.note.service.TagService;
import com.wyl.note.service.UserService;
import com.wyl.note.util.JwtUtil;
import com.wyl.note.vo.UserStatsVO;
import com.wyl.note.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final NoteService noteService;
    private final CategoryService categoryService;
    private final TagService tagService;

    @Override
    public String login(LoginDTO loginDTO) {
        User user = getByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        return jwtUtil.generateToken(user.getId(), user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterDTO registerDTO) {
        User existUser = getByUsername(registerDTO.getUsername());
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setNickname(registerDTO.getNickname() != null ? registerDTO.getNickname() : registerDTO.getUsername());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        save(user);
        return user.getId();
    }

    @Override
    public UserVO getCurrentUser(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    public User getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    public UserStatsVO getUserStats(Long userId) {
        UserStatsVO stats = new UserStatsVO();
        
        long noteCount = noteService.count(new LambdaQueryWrapper<Note>()
                .eq(Note::getUserId, userId));
        stats.setNoteCount(noteCount);
        
        long categoryCount = categoryService.count(new LambdaQueryWrapper<Category>()
                .eq(Category::getUserId, userId));
        stats.setCategoryCount(categoryCount);
        
        long tagCount = tagService.count(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getUserId, userId));
        stats.setTagCount(tagCount);
        
        stats.setErrorQuestionCount(0L);
        
        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateUserInfo(Long userId, UpdateUserInfoDTO updateUserInfoDTO) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (StringUtils.hasText(updateUserInfoDTO.getNickname())) {
            user.setNickname(updateUserInfoDTO.getNickname());
        }
        if (StringUtils.hasText(updateUserInfoDTO.getAvatar())) {
            user.setAvatar(updateUserInfoDTO.getAvatar());
        }
        user.setUpdatedAt(LocalDateTime.now());
        updateById(user);
        
        return getCurrentUser(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, UpdatePasswordDTO updatePasswordDTO) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (!passwordEncoder.matches(updatePasswordDTO.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }
        
        user.setPassword(passwordEncoder.encode(updatePasswordDTO.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateAvatar(Long userId, String avatarUrl) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setAvatar(avatarUrl);
        user.setUpdatedAt(LocalDateTime.now());
        updateById(user);
        
        return getCurrentUser(userId);
    }
}
