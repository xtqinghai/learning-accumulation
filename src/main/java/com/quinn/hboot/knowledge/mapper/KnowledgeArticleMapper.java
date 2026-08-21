package com.quinn.hboot.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quinn.hboot.knowledge.model.entity.KnowledgeArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KnowledgeArticleMapper extends BaseMapper<KnowledgeArticle> {
    Page<KnowledgeArticle> search(
        Page<KnowledgeArticle> page,
        @Param("spaceId") Long spaceId,
        @Param("tokens") List<String> tokens,
        @Param("status") String status
    );
}
