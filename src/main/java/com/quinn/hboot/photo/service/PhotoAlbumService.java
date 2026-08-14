package com.quinn.hboot.photo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.quinn.hboot.photo.model.dto.PhotoRequests;
import com.quinn.hboot.photo.model.entity.PhotoAlbum;
import com.quinn.hboot.photo.model.entity.PhotoSpace;
import com.quinn.hboot.photo.model.vo.PhotoMediaVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PhotoAlbumService {
    PhotoSpace getMySpace();
    List<PhotoAlbum> listAlbums();
    PhotoAlbum createAlbum(PhotoRequests.AlbumSave request);
    PhotoAlbum updateAlbum(Long id, PhotoRequests.AlbumSave request);
    void deleteAlbum(Long id);
    IPage<PhotoMediaVO> pageMedia(Long albumId, String keyword, String mediaType, Long tagId, long page, long size);
    PhotoMediaVO getMedia(Long id);
    PhotoMediaVO upload(MultipartFile file, Long albumId, String relativePath) throws Exception;
    PhotoMediaVO updateMedia(Long id, PhotoRequests.MediaUpdate request);
    void deleteMedia(List<Long> ids);
    void copyOrMove(PhotoRequests.MediaMove request);
    List<com.quinn.hboot.photo.model.entity.PhotoTag> listTags();
    List<com.quinn.hboot.photo.model.entity.PhotoTag> setTags(Long mediaId, List<String> names);
    void writeContent(Long mediaId, HttpServletResponse response, boolean download) throws Exception;
    void exportAlbum(Long albumId, HttpServletResponse response) throws Exception;
}
