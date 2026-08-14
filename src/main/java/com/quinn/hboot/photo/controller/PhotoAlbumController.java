package com.quinn.hboot.photo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.quinn.hboot.common.R;
import com.quinn.hboot.photo.model.dto.PhotoRequests;
import com.quinn.hboot.photo.model.entity.PhotoAlbum;
import com.quinn.hboot.photo.model.entity.PhotoSpace;
import com.quinn.hboot.photo.model.entity.PhotoTag;
import com.quinn.hboot.photo.model.vo.PhotoMediaVO;
import com.quinn.hboot.photo.service.PhotoAlbumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "家庭相册")
@RestController
@RequestMapping("/api/v1/photo")
@RequiredArgsConstructor
public class PhotoAlbumController {
    private final PhotoAlbumService photoAlbumService;

    @Operation(summary = "获取当前用户相册空间")
    @GetMapping("/space")
    public R<PhotoSpace> getSpace() {
        return R.ok(photoAlbumService.getMySpace());
    }

    @Operation(summary = "查询相册")
    @GetMapping("/albums")
    public R<List<PhotoAlbum>> listAlbums() {
        return R.ok(photoAlbumService.listAlbums());
    }

    @Operation(summary = "创建相册")
    @PostMapping("/albums")
    public R<PhotoAlbum> createAlbum(@RequestBody @Valid PhotoRequests.AlbumSave request) {
        return R.ok(photoAlbumService.createAlbum(request));
    }

    @Operation(summary = "修改相册")
    @PutMapping("/albums/{id}")
    public R<PhotoAlbum> updateAlbum(@PathVariable Long id, @RequestBody @Valid PhotoRequests.AlbumSave request) {
        return R.ok(photoAlbumService.updateAlbum(id, request));
    }

    @Operation(summary = "删除相册，媒体保留在全部媒体中")
    @DeleteMapping("/albums/{id}")
    public R<Void> deleteAlbum(@PathVariable Long id) {
        photoAlbumService.deleteAlbum(id);
        return R.ok();
    }

    @Operation(summary = "导出可离线浏览的相册压缩包")
    @GetMapping("/albums/{id}/export")
    public void exportAlbum(@PathVariable Long id, HttpServletResponse response) throws Exception {
        photoAlbumService.exportAlbum(id, response);
    }

    @Operation(summary = "分页查询照片和视频")
    @GetMapping("/media")
    public R<IPage<PhotoMediaVO>> pageMedia(
        @RequestParam(required = false) Long albumId,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String mediaType,
        @RequestParam(required = false) Long tagId,
        @RequestParam(defaultValue = "1") long page,
        @RequestParam(defaultValue = "24") long size
    ) {
        return R.ok(photoAlbumService.pageMedia(albumId, keyword, mediaType, tagId, page, size));
    }

    @Operation(summary = "获取媒体详情")
    @GetMapping("/media/{id}")
    public R<PhotoMediaVO> getMedia(@PathVariable Long id) {
        return R.ok(photoAlbumService.getMedia(id));
    }

    @Operation(summary = "上传照片或视频")
    @PostMapping("/media/upload")
    public R<PhotoMediaVO> upload(
        @RequestPart("file") MultipartFile file,
        @RequestParam(required = false) Long albumId,
        @RequestParam(required = false) String relativePath
    ) throws Exception {
        return R.ok(photoAlbumService.upload(file, albumId, relativePath));
    }

    @Operation(summary = "编辑媒体信息")
    @PutMapping("/media/{id}")
    public R<PhotoMediaVO> updateMedia(@PathVariable Long id, @RequestBody PhotoRequests.MediaUpdate request) {
        return R.ok(photoAlbumService.updateMedia(id, request));
    }

    @Operation(summary = "移入或复制媒体到相册")
    @PostMapping("/media/transfer")
    public R<Void> transfer(@RequestBody @Valid PhotoRequests.MediaMove request) {
        photoAlbumService.copyOrMove(request);
        return R.ok();
    }

    @Operation(summary = "删除媒体")
    @DeleteMapping("/media")
    public R<Void> deleteMedia(@RequestBody List<Long> ids) {
        photoAlbumService.deleteMedia(ids);
        return R.ok();
    }

    @Operation(summary = "查询标签")
    @GetMapping("/tags")
    public R<List<PhotoTag>> listTags() {
        return R.ok(photoAlbumService.listTags());
    }

    @Operation(summary = "设置媒体标签")
    @PutMapping("/media/{id}/tags")
    public R<List<PhotoTag>> setTags(@PathVariable Long id, @RequestBody @Valid PhotoRequests.TagSave request) {
        return R.ok(photoAlbumService.setTags(id, request.tags()));
    }

    @Operation(summary = "预览媒体原文件")
    @GetMapping("/media/{id}/content")
    public void content(@PathVariable Long id, HttpServletResponse response) throws Exception {
        photoAlbumService.writeContent(id, response, false);
    }

    @Operation(summary = "下载媒体原文件")
    @GetMapping("/media/{id}/download")
    public void download(@PathVariable Long id, HttpServletResponse response) throws Exception {
        photoAlbumService.writeContent(id, response, true);
    }
}
