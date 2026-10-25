package com.quinn.hboot.family.model.entity;
import com.baomidou.mybatisplus.annotation.*; import lombok.Data; import java.math.BigDecimal; import java.time.LocalDateTime;
@Data @TableName("photo_duplicate_candidate") public class PhotoDuplicateCandidate { @TableId(type=IdType.AUTO) private Long id; private Long spaceId; private Long mediaId; private Long duplicateMediaId; private String matchType; private BigDecimal confidence; private Integer marked; private LocalDateTime createTime; }
