package com.quinn.hboot.notification.model.query;

import com.quinn.hboot.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "我的消息查询对象")
public class NotificationQuery extends BasePageQuery {

    @Schema(description = "消息类型")
    private String type;

    @Schema(description = "已读状态，0 未读，1 已读")
    private Integer readStatus;
}
