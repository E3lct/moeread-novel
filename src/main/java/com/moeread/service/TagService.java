package com.moeread.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.moeread.entity.Tag;

import java.util.List;

public interface TagService extends IService<Tag> {

    /** 用户的所有标签 */
    List<Tag> listTags(Integer userId);

    /** 创建标签 */
    Tag createTag(Integer userId, String name, String color);

    /** 删除标签 */
    void deleteTag(Integer userId, Integer tagId);

    /** 给书籍打标签 */
    void addBookTag(Integer userId, Integer bookId, Integer tagId);

    /** 移除书籍标签 */
    void removeBookTag(Integer userId, Integer bookId, Integer tagId);
}
