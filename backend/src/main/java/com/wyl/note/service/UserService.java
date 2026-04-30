package com.wyl.note.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wyl.note.dto.LoginDTO;
import com.wyl.note.dto.RegisterDTO;
import com.wyl.note.dto.UpdatePasswordDTO;
import com.wyl.note.dto.UpdateUserInfoDTO;
import com.wyl.note.entity.User;
import com.wyl.note.vo.UserStatsVO;
import com.wyl.note.vo.UserVO;

public interface UserService extends IService<User> {

    String login(LoginDTO loginDTO);

    Long register(RegisterDTO registerDTO);

    UserVO getCurrentUser(Long userId);

    User getByUsername(String username);

    UserStatsVO getUserStats(Long userId);

    UserVO updateUserInfo(Long userId, UpdateUserInfoDTO updateUserInfoDTO);

    void updatePassword(Long userId, UpdatePasswordDTO updatePasswordDTO);

    UserVO updateAvatar(Long userId, String avatarUrl);
}
