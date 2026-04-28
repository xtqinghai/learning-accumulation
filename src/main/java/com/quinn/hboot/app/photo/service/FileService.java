/*
package com.quinn.hboot.app.photo.service;

import com.quinn.hboot.utils.ExifUtil;
import com.quinn.hboot.utils.HashUtil;
import com.quinn.hboot.utils.ImageUtil;
import com.quinn.hboot.utils.VideoUtil;
import org.ehcache.shadow.org.terracotta.offheapstore.Metadata;

import java.io.File;

public class FileService {

    public void process(File file, String thumbDir, AlbumService albumService) throws Exception {

        String hash = HashUtil.md5(file);

        if (DB.existsByHash(hash)) {
            return; // 去重
        }

        String ext = getExt(file.getName());

        boolean isImage = isImage(ext);
        boolean isVideo = isVideo(ext);

        String thumbPath;

        if (isImage) {
            thumbPath = ImageUtil.createThumbnail(file, thumbDir);
        } else if (isVideo) {
            thumbPath = VideoUtil.createThumbnail(file, thumbDir);
        } else {
            return;
        }

        Metadata metadata = isImage ? ExifUtil.parse(file) : new Metadata();

        long fileId = DB.insertFile(file, hash, thumbPath, metadata, isVideo);

        long albumId = albumService.getOrCreateAlbum(file);

        DB.insertAlbumFile(albumId, fileId);
    }

    private boolean isImage(String ext) {
        return ext.matches("(?i)jpg|jpeg|png|webp");
    }

    private boolean isVideo(String ext) {
        return ext.matches("(?i)mp4|mov|avi|mkv");
    }

    private String getExt(String name) {
        return name.substring(name.lastIndexOf('.') + 1);
    }
}
*/
