package com.quinn.hboot.schedule.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quinn.hboot.schedule.model.entity.ScheduledJobLog;
import com.quinn.hboot.schedule.model.query.ScheduledJobLogQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ScheduledJobLogMapper extends BaseMapper<ScheduledJobLog> {

    Page<ScheduledJobLog> selectLogPage(Page<ScheduledJobLog> page, @Param("query") ScheduledJobLogQuery query);
}
