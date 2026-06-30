package com.moeread.controller;

import com.moeread.common.Result;
import com.moeread.config.LoginUser;
import com.moeread.config.RequestContext;
import com.moeread.entity.Tag;
import com.moeread.service.TagService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tag")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    /** 用户标签列表 */
    @LoginUser
    @GetMapping("/list")
    public Result<List<Tag>> list() {
        Integer userId = RequestContext.getUserId();
        return Result.ok(tagService.listTags(userId));
    }

    /** 创建标签 */
    @LoginUser
    @PostMapping
    public Result<Tag> create(@RequestBody Map<String, String> body) {
        Integer userId = RequestContext.getUserId();
        Tag tag = tagService.createTag(userId, body.get("name"), body.get("color"));
        return Result.ok(tag);
    }

    /** 删除标签 */
    @LoginUser
    @DeleteMapping("/{tagId}")
    public Result<?> delete(@PathVariable Integer tagId) {
        Integer userId = RequestContext.getUserId();
        tagService.deleteTag(userId, tagId);
        return Result.ok();
    }

    /** 给书籍打标签 */
    @LoginUser
    @PostMapping("/book")
    public Result<?> addBookTag(@RequestBody Map<String, Integer> body) {
        Integer userId = RequestContext.getUserId();
        tagService.addBookTag(userId, body.get("bookId"), body.get("tagId"));
        return Result.ok();
    }

    /** 移除书籍标签 */
    @LoginUser
    @DeleteMapping("/book")
    public Result<?> removeBookTag(@RequestBody Map<String, Integer> body) {
        Integer userId = RequestContext.getUserId();
        tagService.removeBookTag(userId, body.get("bookId"), body.get("tagId"));
        return Result.ok();
    }
}
