package com.moeread.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moeread.config.JwtConfig;
import com.moeread.dto.LoginVO;
import com.moeread.dto.RegisterDTO;
import com.moeread.dto.UserVO;
import com.moeread.entity.User;
import com.moeread.mapper.UserMapper;
import com.moeread.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtConfig jwtConfig;

    public UserServiceImpl(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @Override
    public Integer register(RegisterDTO dto) {
        // 检查用户名是否已存在
        long count = count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(md5(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setDailyGoal(30);
        save(user);
        return user.getId();
    }

    @Override
    public LoginVO login(String username, String password) {
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (!md5(password).equals(user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        String token = jwtConfig.generateToken(user.getId(), user.getUsername());
        return new LoginVO(token, user.getId(), user.getUsername(), user.getNickname());
    }

    @Override
    public UserVO getProfile(Integer userId) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    public void changePassword(Integer userId, String oldPwd, String newPwd) {
        User user = getById(userId);
        if (!md5(oldPwd).equals(user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(md5(newPwd));
        updateById(user);
    }

    @Override
    public void updateProfile(Integer userId, UserVO vo) {
        User user = getById(userId);
        if (vo.getNickname() != null) user.setNickname(vo.getNickname());
        if (vo.getAvatar() != null) user.setAvatar(vo.getAvatar());
        if (vo.getDailyGoal() != null) user.setDailyGoal(vo.getDailyGoal());
        if (vo.getMascotImage() != null) user.setMascotImage(vo.getMascotImage());
        if (vo.getMascotOpacity() != null) user.setMascotOpacity(vo.getMascotOpacity());
        if (vo.getThemeMode() != null) user.setThemeMode(vo.getThemeMode());
        if (vo.getBgScale() != null) user.setBgScale(vo.getBgScale());
        if (vo.getBgMirror() != null) user.setBgMirror(vo.getBgMirror());
        updateById(user);
    }

    /** MD5 加密，和作业版保持一致 */
    private String md5(String s) {
        return DigestUtils.md5DigestAsHex(s.getBytes(StandardCharsets.UTF_8));
    }
}
