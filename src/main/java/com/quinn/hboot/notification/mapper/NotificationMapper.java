package com.quinn.hboot.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quinn.hboot.notification.model.entity.SysNotification;
import com.quinn.hboot.notification.model.query.NotificationQuery;
import com.quinn.hboot.notification.model.vo.NotificationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NotificationMapper extends BaseMapper<SysNotification> {

    Page<NotificationVO> selectRecipientPage(Page<NotificationVO> page,
                                             @Param("recipientId") Long recipientId,
                                             @Param("query") NotificationQuery query);
}
