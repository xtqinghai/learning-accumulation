package com.quinn.hboot.family.model.entity;
import com.baomidou.mybatisplus.annotation.*; import lombok.Data; import java.time.LocalDateTime;
@Data @TableName("family_device") public class FamilyDevice { @TableId(type=IdType.AUTO) private Long id; private Long spaceId; private Long userId; private String deviceName; private String deviceType; private String deviceKey; private String syncCursor; private LocalDateTime lastSeenAt; private Integer status; private LocalDateTime createTime; private LocalDateTime updateTime; }
