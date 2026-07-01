package com.moeread.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.moeread.dto.BookSourceDTO;
import com.moeread.dto.SourceImportDTO;
import com.moeread.entity.Book;
import com.moeread.entity.BookSource;

import java.util.List;

public interface BookSourceService extends IService<BookSource> {
    List<BookSourceDTO> listPresets();

    List<BookSourceDTO> listUserSources(Integer userId);

    BookSourceDTO addSource(Integer userId, BookSourceDTO dto);

    BookSourceDTO addPreset(Integer userId, String sourceKey);

    void updateEnabled(Integer userId, Integer sourceId, boolean enabled);

    void deleteSource(Integer userId, Integer sourceId);

    Book importFromSource(Integer userId, SourceImportDTO dto);
}
