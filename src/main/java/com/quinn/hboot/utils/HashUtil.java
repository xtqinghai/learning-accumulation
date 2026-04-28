package com.quinn.hboot.utils;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

public class HashUtil {

    public static String md5(File file) throws Exception {

        MessageDigest md = MessageDigest.getInstance("MD5");

        FileInputStream fis = new FileInputStream(file);

        byte[] buffer = new byte[8192];
        int read;

        while ((read = fis.read(buffer)) != -1) {
            md.update(buffer, 0, read);
        }

        fis.close();

        byte[] digest = md.digest();

        StringBuilder sb = new StringBuilder();

        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }
}
