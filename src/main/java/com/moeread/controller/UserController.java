package com.moeread.controller;

import com.moeread.common.Result;
import com.moeread.config.LoginUser;
import com.moeread.config.RequestContext;
import com.moeread.dto.LoginDTO;
import com.moeread.dto.LoginVO;
import com.moeread.dto.RegisterDTO;
import com.moeread.dto.UserVO;
import com.moeread.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Value("${moeread.upload.path}")
    private String uploadPath;

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

    @LoginUser
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        Integer userId = RequestContext.getUserId();
        if (file == null || file.isEmpty()) {
            return Result.error(400, "图片不能为空");
        }
        try {
            Path dir = Paths.get(uploadPath).toAbsolutePath().resolve("profile");
            Files.createDirectories(dir);
            String original = file.getOriginalFilename();
            String ext = ".jpg";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf("."));
            }
            String filename = userId + "_" + System.currentTimeMillis() + ext;
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            }
            return Result.ok(Map.of("url", "/uploads/profile/" + filename));
        } catch (IOException e) {
            return Result.error(500, "图片上传失败: " + e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "图片上传失败: " + e.getMessage());
        }
    }
}
