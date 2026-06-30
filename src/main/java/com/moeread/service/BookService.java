package com.moeread.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.moeread.entity.Book;
import com.moeread.dto.BookVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BookService extends IService<Book> {

    /** 书架列表（含进度、标签） */
    List<BookVO> listBooks(Integer userId);

    /** 搜索书架 */
    List<BookVO> searchBooks(Integer userId, String keyword);

    /** 导入单个 TXT 文件 */
    Book importTxt(Integer userId, MultipartFile file);

    /** 导入 ZIP 批量 */
    List<Book> importZip(Integer userId, MultipartFile file);

    /** 编辑书籍信息（标题、封面等） */
    void updateBook(Integer userId, Integer bookId, BookVO vo);

    /** 删除书籍 */
    void deleteBook(Integer userId, Integer bookId);

    /** 上传封面图片，返回路径 */
    String uploadCover(Integer userId, MultipartFile file);
}
