package com.quinn.hboot.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quinn.hboot.knowledge.model.entity.KnowledgeArticleChunk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KnowledgeArticleChunkMapper extends BaseMapper<KnowledgeArticleChunk> {
    List<KnowledgeArticleChunk> searchRelevant(
        @Param("spaceId") Long spaceId,
        @Param("tokens") List<String> tokens,
        @Param("status") String status,
        @Param("limit") int limit
    );
}

