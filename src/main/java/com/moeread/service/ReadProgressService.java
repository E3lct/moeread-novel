package com.moeread.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.moeread.entity.ReadProgress;

public interface ReadProgressService extends IService<ReadProgress> {

    /** 保存阅读进度 */
    void saveProgress(Integer userId, Integer bookId, Integer chapterIndex, Integer scrollPosition);

    /** 获取某书的阅读进度 */
    ReadProgress getProgress(Integer userId, Integer bookId);
}
