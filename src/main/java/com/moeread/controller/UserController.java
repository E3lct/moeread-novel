package com.moeread.controller;

import com.moeread.common.Result;
import com.moeread.config.LoginUser;
import com.moeread.config.RequestContext;
import com.moeread.dto.LoginDTO;
import com.moeread.dto.LoginVO;
import com.moeread.dto.RegisterDTO;
import com.moeread.dto.UserVO;
import com.moeread.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** 注册 */
    @PostMapping("/register")
    public Result<Integer> register(@RequestBody RegisterDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            return Result.error(400, "用户名不能为空");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            return Result.error(400, "密码至少6位");
        }
        Integer userId = userService.register(dto);
        return Result.ok(userId);
    }

    /** 登录 */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto) {
        LoginVO vo = userService.login(dto.getUsername(), dto.getPassword());
        return Result.ok(vo);
    }

    /** 获取个人信息 */
    @LoginUser
    @GetMapping("/profile")
    public Result<UserVO> profile() {
        Integer userId = RequestContext.getUserId();
        UserVO vo = userService.getProfile(userId);
        return Result.ok(vo);
    }

    /** 修改个人信息 */
    @LoginUser
    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody UserVO vo) {
        Integer userId = RequestContext.getUserId();
        userService.updateProfile(userId, vo);
        return Result.ok();
    }

    /** 修改密码 */
    @LoginUser
    @PutMapping("/password")
    public Result<?> changePassword(@RequestBody Map<String, String> body) {
        Integer userId = RequestContext.getUserId();
        userService.changePassword(userId, body.get("oldPassword"), body.get("newPassword"));
        return Result.ok();
    }
}
