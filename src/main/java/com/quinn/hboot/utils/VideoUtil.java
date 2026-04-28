package com.quinn.hboot.utils;

import java.io.File;

public class VideoUtil {

    public static String createThumbnail(File file, String thumbDir) throws Exception {

        String outPath = thumbDir + "/" + file.getName() + ".jpg";

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-ss", "00:00:01",
                "-i", file.getAbsolutePath(),
                "-vframes", "1",
                outPath
        );

        pb.start().waitFor();

        return outPath;
    }
}
