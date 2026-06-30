package com.moeread.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.moeread.entity.Chapter;
import com.moeread.dto.ChapterVO;

import java.util.List;

public interface ChapterService extends IService<Chapter> {

    /** 获取某书的章节目录（仅标题，不含内容） */
    List<ChapterVO> getToc(Integer bookId);

    /** 获取章节内容 */
    ChapterVO getContent(Integer chapterId);

    /** 获取某书的总章节数 */
    int countByBook(Integer bookId);
}
