package com.moeread.controller;

import com.moeread.common.Result;
import com.moeread.config.LoginUser;
import com.moeread.config.RequestContext;
import com.moeread.dto.BookVO;
import com.moeread.entity.Book;
import com.moeread.service.BookService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/book")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /** 书架列表 */
    @LoginUser
    @GetMapping("/list")
    public Result<List<BookVO>> list() {
        Integer userId = RequestContext.getUserId();
        return Result.ok(bookService.listBooks(userId));
    }

    /** 搜索 */
    @LoginUser
    @GetMapping("/search")
    public Result<List<BookVO>> search(@RequestParam String keyword) {
        Integer userId = RequestContext.getUserId();
        return Result.ok(bookService.searchBooks(userId, keyword));
    }

    /** 导入 TXT */
    @LoginUser
    @PostMapping("/import/txt")
    public Result<Book> importTxt(@RequestParam("file") MultipartFile file) {
        Integer userId = RequestContext.getUserId();
        Book book = bookService.importTxt(userId, file);
        return Result.ok(book);
    }

    /** 导入 ZIP */
    @LoginUser
    @PostMapping("/import/zip")
    public Result<List<Book>> importZip(@RequestParam("file") MultipartFile file) {
        Integer userId = RequestContext.getUserId();
        return Result.ok(bookService.importZip(userId, file));
    }

    /** 编辑书籍 */
    @LoginUser
    @PutMapping("/{bookId}")
    public Result<?> updateBook(@PathVariable Integer bookId, @RequestBody BookVO vo) {
        Integer userId = RequestContext.getUserId();
        bookService.updateBook(userId, bookId, vo);
        return Result.ok();
    }

    /** 删除书籍 */
    @LoginUser
    @DeleteMapping("/{bookId}")
    public Result<?> deleteBook(@PathVariable Integer bookId) {
        Integer userId = RequestContext.getUserId();
        bookService.deleteBook(userId, bookId);
        return Result.ok();
    }

    /** 上传封面 */
    @LoginUser
    @PostMapping("/cover")
    public Result<Map<String, String>> uploadCover(@RequestParam("file") MultipartFile file) {
        Integer userId = RequestContext.getUserId();
        String path = bookService.uploadCover(userId, file);
        return Result.ok(Map.of("url", path));
    }
}
