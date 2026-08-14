package com.quinn.hboot.photo.model.vo;

import com.quinn.hboot.photo.model.entity.PhotoMedia;
import com.quinn.hboot.photo.model.entity.PhotoTag;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PhotoMediaVO extends PhotoMedia {
    private String fileName;
    private String contentType;
    private Long fileSize;
    private List<PhotoTag> tags;
}
