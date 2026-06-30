package com.moeread.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moeread.dto.ChapterVO;
import com.moeread.entity.Chapter;
import com.moeread.mapper.ChapterMapper;
import com.moeread.service.ChapterService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChapterServiceImpl extends ServiceImpl<ChapterMapper, Chapter> implements ChapterService {

    @Override
    public List<ChapterVO> getToc(Integer bookId) {
        List<Chapter> chapters = list(new LambdaQueryWrapper<Chapter>()
                .eq(Chapter::getBookId, bookId)
                .orderByAsc(Chapter::getChapterIndex));
        return chapters.stream().map(ch -> {
            ChapterVO vo = new ChapterVO();
            BeanUtils.copyProperties(ch, vo);
            vo.setContent(null); // 目录不返回内容
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public ChapterVO getContent(Integer chapterId) {
        Chapter chapter = getById(chapterId);
        if (chapter == null) throw new RuntimeException("章节不存在");
        ChapterVO vo = new ChapterVO();
        BeanUtils.copyProperties(chapter, vo);
        return vo;
    }

    @Override
    public int countByBook(Integer bookId) {
        return (int) count(new LambdaQueryWrapper<Chapter>().eq(Chapter::getBookId, bookId));
    }
}
