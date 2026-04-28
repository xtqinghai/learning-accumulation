package com.quinn.hboot.utils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;
import com.quinn.hboot.app.photo.domain.PhotoMeta;

import java.io.File;
import java.util.Date;

public class ExifUtil {

    public static PhotoMeta parse(File file) {

        PhotoMeta result = new PhotoMeta();

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);

            // 时间
            ExifSubIFDDirectory exif =
                    metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

            if (exif != null) {
                result.setTakenTime(exif.getDateOriginal());
            }

            // GPS
            GpsDirectory gpsDir =
                    metadata.getFirstDirectoryOfType(GpsDirectory.class);

            if (gpsDir != null) {
                GeoLocation geo = gpsDir.getGeoLocation();
                if (geo != null && !geo.isZero()) {
                    result.setLat(geo.getLatitude());
                    result.setLng(geo.getLongitude());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}