package com.quinn.hboot.family.model.entity;
import com.baomidou.mybatisplus.annotation.*; import lombok.Data; import java.time.LocalDateTime;
@Data @TableName("family_sync_log") public class FamilySyncLog { @TableId(type=IdType.AUTO) private Long id; private Long deviceId; private String direction; private String entityType; private Long entityId; private String operation; private String conflictStatus; private String message; private LocalDateTime createTime; }
