package com.quinn.hboot.family.model.entity;
import com.baomidou.mybatisplus.annotation.*; import lombok.Data; import java.time.LocalDateTime;
@Data @TableName("family_share_link") public class FamilyShareLink { @TableId(type=IdType.AUTO) private Long id; private Long spaceId; private Long createdBy; private String resourceType; private Long resourceId; private String token; private LocalDateTime expiresAt; private Integer status; private LocalDateTime createTime; }
