package com.quinn.hboot.photo.model.entity;
import com.baomidou.mybatisplus.annotation.*; import lombok.Data; import java.time.LocalDateTime;
@Data @TableName("photo_media_derivative") public class PhotoMediaDerivative { @TableId(type=IdType.AUTO) private Long id; private Long mediaId; private String type; private Long fileId; private Integer width; private Integer height; private Integer status; private LocalDateTime createTime; }
