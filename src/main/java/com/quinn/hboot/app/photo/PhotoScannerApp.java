package com.quinn.hboot.app.photo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PhotoScannerApp {

    public static void main(String[] args) {
        String rootDir = "F:\\个人文件\\照片";
        String thumbDir = "F:\\个人文件\\照片缩略";

        log.info("Photo scanner skipped, rootDir={}, thumbDir={}", rootDir, thumbDir);
    }
}
