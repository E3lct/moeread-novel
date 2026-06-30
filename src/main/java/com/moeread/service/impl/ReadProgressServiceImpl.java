package com.moeread.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moeread.entity.Chapter;
import com.moeread.entity.ReadProgress;
import com.moeread.mapper.ChapterMapper;
import com.moeread.mapper.ReadProgressMapper;
import com.moeread.service.ReadProgressService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReadProgressServiceImpl extends ServiceImpl<ReadProgressMapper, ReadProgress>
        implements ReadProgressService {

    private final ChapterMapper chapterMapper;

    public ReadProgressServiceImpl(ChapterMapper chapterMapper) {
        this.chapterMapper = chapterMapper;
    }

    @Override
    public void saveProgress(Integer userId, Integer bookId, Integer chapterIndex, Integer scrollPosition) {
        ReadProgress progress = getOne(new LambdaQueryWrapper<ReadProgress>()
                .eq(ReadProgress::getUserId, userId)
                .eq(ReadProgress::getBookId, bookId));

        if (progress == null) {
            progress = new ReadProgress();
            progress.setUserId(userId);
            progress.setBookId(bookId);
        }

        progress.setChapterIndex(chapterIndex);
        progress.setScrollPosition(scrollPosition != null ? scrollPosition : 0);
        progress.setUpdateTime(LocalDateTime.now());

        // 计算百分比
        long totalChapters = chapterMapper.selectCount(
                new LambdaQueryWrapper<Chapter>().eq(Chapter::getBookId, bookId));
        if (totalChapters > 0) {
            progress.setProgressPercent((int) ((chapterIndex + 1) * 100 / totalChapters));
        }

        saveOrUpdate(progress);
    }

    @Override
    public ReadProgress getProgress(Integer userId, Integer bookId) {
        return getOne(new LambdaQueryWrapper<ReadProgress>()
                .eq(ReadProgress::getUserId, userId)
                .eq(ReadProgress::getBookId, bookId));
    }
}
