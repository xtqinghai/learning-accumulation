/*
package com.quinn.hboot.app.photo.service;

import com.quinn.hboot.system.service.FileService;

import java.io.File;
import java.nio.file.*;
import java.util.concurrent.*;

public class ScannerService {

    private final String rootDir;
    private final String thumbDir;t

    private final ExecutorService pool = Executors.newFixedThreadPool(8);

    private final FileService fileService = new FileService();
    private final AlbumService albumService = new AlbumService();

    public ScannerService(String rootDir, String thumbDir) {
        this.rootDir = rootDir;
        this.thumbDir = thumbDir;
    }

    public void scan() throws Exception {

        Files.walk(Paths.get(rootDir))
                .filter(Files::isRegularFile)
                .forEach(path -> pool.submit(() -> processFile(path.toFile())));

        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.HOURS);
    }

    private void processFile(File file) {
        try {
            fileService.process(file, thumbDir, albumService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
*/
