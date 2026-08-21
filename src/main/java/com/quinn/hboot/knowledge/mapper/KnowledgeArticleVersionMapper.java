package com.quinn.hboot.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quinn.hboot.knowledge.model.entity.KnowledgeArticleVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface KnowledgeArticleVersionMapper extends BaseMapper<KnowledgeArticleVersion> {
    @Select("SELECT COALESCE(MAX(version_no), 0) FROM kb_article_version WHERE article_id = #{articleId}")
    int selectMaxVersionNo(@Param("articleId") Long articleId);
}
