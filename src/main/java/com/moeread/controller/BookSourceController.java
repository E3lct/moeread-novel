package com.moeread.controller;

import com.moeread.common.Result;
import com.moeread.config.LoginUser;
import com.moeread.config.RequestContext;
import com.moeread.dto.BookSourceDTO;
import com.moeread.dto.SourceImportDTO;
import com.moeread.entity.Book;
import com.moeread.service.BookSourceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/source")
public class BookSourceController {

    private final BookSourceService sourceService;

    public BookSourceController(BookSourceService sourceService) {
        this.sourceService = sourceService;
    }

    @LoginUser
    @GetMapping("/presets")
    public Result<List<BookSourceDTO>> presets() {
        return Result.ok(sourceService.listPresets());
    }

    @LoginUser
    @GetMapping("/list")
    public Result<List<BookSourceDTO>> list() {
        return Result.ok(sourceService.listUserSources(RequestContext.getUserId()));
    }

    @LoginUser
    @PostMapping
    public Result<BookSourceDTO> add(@RequestBody BookSourceDTO dto) {
        return Result.ok(sourceService.addSource(RequestContext.getUserId(), dto));
    }

    @LoginUser
    @PostMapping("/preset/{sourceKey}")
    public Result<BookSourceDTO> addPreset(@PathVariable String sourceKey) {
        return Result.ok(sourceService.addPreset(RequestContext.getUserId(), sourceKey));
    }

    @LoginUser
    @PutMapping("/{sourceId}/enabled")
    public Result<?> enabled(@PathVariable Integer sourceId, @RequestBody Map<String, Boolean> body) {
        sourceService.updateEnabled(RequestContext.getUserId(), sourceId, Boolean.TRUE.equals(body.get("enabled")));
        return Result.ok();
    }

    @LoginUser
    @DeleteMapping("/{sourceId}")
    public Result<?> delete(@PathVariable Integer sourceId) {
        sourceService.deleteSource(RequestContext.getUserId(), sourceId);
        return Result.ok();
    }

    @LoginUser
    @PostMapping("/import")
    public Result<Book> importFromSource(@RequestBody SourceImportDTO dto) {
        return Result.ok(sourceService.importFromSource(RequestContext.getUserId(), dto));
    }
}
