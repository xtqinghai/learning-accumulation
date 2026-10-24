package com.quinn.hboot.schedule.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quinn.hboot.schedule.model.entity.ScheduledJob;
import com.quinn.hboot.schedule.model.query.ScheduledJobQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ScheduledJobMapper extends BaseMapper<ScheduledJob> {

    Page<ScheduledJob> selectJobPage(Page<ScheduledJob> page, @Param("query") ScheduledJobQuery query);
}
