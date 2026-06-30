package com.moeread.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.moeread.entity.User;
import com.moeread.dto.LoginVO;
import com.moeread.dto.RegisterDTO;
import com.moeread.dto.UserVO;

public interface UserService extends IService<User> {

    /** 注册，返回用户 ID */
    Integer register(RegisterDTO dto);

    /** 登录，返回 token + 用户信息 */
    LoginVO login(String username, String password);

    /** 获取当前用户信息 */
    UserVO getProfile(Integer userId);

    /** 修改密码 */
    void changePassword(Integer userId, String oldPwd, String newPwd);

    /** 修改个人信息 */
    void updateProfile(Integer userId, UserVO vo);
}
