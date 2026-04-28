/*
package com.quinn.hboot.app.photo.service;

import java.io.File;

public class AlbumService {

    public long getOrCreateAlbum(File file) {

        String dir = file.getParent();

        Long id = DB.findAlbumByDir(dir);

        if (id != null) {
            return id;
        }

        String name = parseName(dir);
        String date = parseDate(dir);

        return DB.insertAlbum(name, date, dir);
    }

    private String parseName(String dir) {
        return new File(dir).getName();
    }

    private String parseDate(String dir) {
        String name = parseName(dir);
        return name.substring(0, 10); // yyyy-MM-dd
    }
}
*/
