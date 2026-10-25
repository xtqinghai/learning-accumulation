package com.quinn.hboot.family.model.entity;
import com.baomidou.mybatisplus.annotation.*; import lombok.Data; import java.time.LocalDateTime;
@Data @TableName("family_document_version") public class FamilyDocumentVersion { @TableId(type=IdType.AUTO) private Long id; private Long documentId; private Long fileId; private Integer versionNo; private String changeNote; private Long createdBy; private LocalDateTime createTime; }
