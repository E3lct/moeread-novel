package com.moeread.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moeread.entity.BookTag;
import com.moeread.entity.Tag;
import com.moeread.mapper.BookTagMapper;
import com.moeread.mapper.TagMapper;
import com.moeread.service.TagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    private final BookTagMapper bookTagMapper;

    public TagServiceImpl(BookTagMapper bookTagMapper) {
        this.bookTagMapper = bookTagMapper;
    }

    @Override
    public List<Tag> listTags(Integer userId) {
        return list(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getUserId, userId)
                .orderByAsc(Tag::getIsSystem)
                .orderByAsc(Tag::getCreateTime));
    }

    @Override
    public Tag createTag(Integer userId, String name, String color) {
        Tag tag = new Tag();
        tag.setUserId(userId);
        tag.setName(name);
        tag.setColor(color != null ? color : "#F59E0B");
        tag.setIsSystem(0);
        save(tag);
        return tag;
    }

    @Override
    @Transactional
    public void deleteTag(Integer userId, Integer tagId) {
        Tag tag = getOne(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getId, tagId).eq(Tag::getUserId, userId));
        if (tag == null) throw new RuntimeException("标签不存在");
        // 删除关联
        bookTagMapper.delete(new LambdaQueryWrapper<BookTag>().eq(BookTag::getTagId, tagId));
        removeById(tagId);
    }

    @Override
    public void addBookTag(Integer userId, Integer bookId, Integer tagId) {
        // 检查是否已存在
        long count = bookTagMapper.selectCount(new LambdaQueryWrapper<BookTag>()
                .eq(BookTag::getBookId, bookId).eq(BookTag::getTagId, tagId));
        if (count > 0) return;

        BookTag bt = new BookTag();
        bt.setBookId(bookId);
        bt.setTagId(tagId);
        bookTagMapper.insert(bt);
    }

    @Override
    public void removeBookTag(Integer userId, Integer bookId, Integer tagId) {
        bookTagMapper.delete(new LambdaQueryWrapper<BookTag>()
                .eq(BookTag::getBookId, bookId).eq(BookTag::getTagId, tagId));
    }
}
