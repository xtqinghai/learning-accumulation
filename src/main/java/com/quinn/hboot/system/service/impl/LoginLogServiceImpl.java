package com.quinn.hboot.system.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quinn.hboot.common.trace.TraceContext;
import com.quinn.hboot.system.converter.LoginLogConverter;
import com.quinn.hboot.system.mapper.LoginLogMapper;
import com.quinn.hboot.system.model.entity.LoginLog;
import com.quinn.hboot.system.model.form.LoginLogForm;
import com.quinn.hboot.system.model.query.LoginLogQuery;
import com.quinn.hboot.system.model.vo.LoginLogVO;
import com.quinn.hboot.system.service.LoginLogService;
import com.quinn.hboot.utils.HttpUtils;
import com.quinn.hboot.utils.IPUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoginLogServiceImpl extends ServiceImpl<LoginLogMapper, LoginLog> implements LoginLogService {

    private final LoginLogConverter loginLogConverter;

    @Override
    public IPage<LoginLogVO> getLoginLogPage(LoginLogQuery queryParams) {
        Page<LoginLogVO> pageVO = this.baseMapper.getLoginLogPage(queryParams.buildPage(), queryParams);
        return pageVO;
    }

    @Override
    public LoginLogForm getLoginLogFormData(Long id) {
        LoginLog entity = this.getById(id);
        return loginLogConverter.toForm(entity);
    }

    @Override
    public boolean saveLoginLog(LoginLogForm formData) {
        LoginLog entity = loginLogConverter.toEntity(formData);
        return this.save(entity);
    }

    @Override
    public boolean updateLoginLog(Long id, LoginLogForm formData) {
        LoginLog entity = loginLogConverter.toEntity(formData);
        entity.setId(id);
        return this.updateById(entity);
    }

    @Override
    public boolean deleteLoginLogs(String ids) {
        Assert.isTrue(StrUtil.isNotBlank(ids), "删除的系统登录日志数据不能为空");
        List<Long> idList = Arrays.stream(ids.split(","))
            .map(Long::parseLong)
            .toList();
        return this.removeByIds(idList);
    }

    @Override
    public boolean record(HttpServletRequest request, String username, boolean success, String msg) {
        String ip = IPUtils.getIpAddr(request);
        String location = null;
        if (StrUtil.isNotBlank(ip)) {
            String region = IPUtils.getRegion(ip);
            if (StrUtil.isNotBlank(region)) {
                String[] regionArray = region.split("\\|");
                if (regionArray.length > 3) {
                    location = regionArray[2] + "|" + regionArray[3];
                }
            }
        }

        LoginLog loginLog = new LoginLog();
        loginLog.setUsername(username);
        loginLog.setStatus(success ? 1 : 0);
        loginLog.setMsg(msg);
        loginLog.setIp(ip);
        loginLog.setLocation(location);
        loginLog.setBrowser(HttpUtils.getBrowserName(request) + "|" + HttpUtils.getBrowserVersion(request));
        loginLog.setOs(HttpUtils.getOsName(request));
        loginLog.setLoginTime(LocalDateTime.now());

        log.info(
            "audit-login traceId={} username={} success={} ip={} message={}",
            TraceContext.getTraceId(),
            username,
            success,
            ip,
            msg
        );
        return this.save(loginLog);
    }
}
