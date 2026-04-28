package com.quinn.hboot.utils;

import net.coobird.thumbnailator.Thumbnails;

import java.io.File;

public class ImageUtil {

    public static String createThumbnail(File file, String thumbDir) throws Exception {

        String outPath = thumbDir + "/" + file.getName();

        Thumbnails.of(file)
                .size(400, 400)
                .toFile(outPath);

        return outPath;
    }
}
