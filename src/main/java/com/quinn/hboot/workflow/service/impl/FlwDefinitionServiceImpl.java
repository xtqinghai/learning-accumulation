package com.quinn.hboot.workflow.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.IoUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quinn.hboot.common.exception.BusinessException;
import com.quinn.hboot.utils.JsonUtils;
import com.quinn.hboot.utils.StringUtils;
import com.quinn.hboot.workflow.common.ConditionalOnEnable;
import com.quinn.hboot.workflow.common.constant.FlowConstant;
import com.quinn.hboot.workflow.domain.FlowCategory;
import com.quinn.hboot.workflow.domain.PageQuery;
import com.quinn.hboot.workflow.domain.vo.FlowDefinitionVo;
import com.quinn.hboot.workflow.mapper.FlwCategoryMapper;
import com.quinn.hboot.workflow.service.IFlwCommonService;
import com.quinn.hboot.workflow.service.IFlwDefinitionService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.dromara.common.core.exception.ServiceException;
//import org.dromara.common.core.utils.StreamUtils;
//import org.dromara.common.core.utils.StringUtils;
//import org.dromara.common.json.utils.JsonUtils;
//import org.dromara.common.mybatis.core.page.PageQuery;
//import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.warm.flow.core.FlowEngine;
import org.dromara.warm.flow.core.dto.DefJson;
import org.dromara.warm.flow.core.enums.NodeType;
import org.dromara.warm.flow.core.enums.PublishStatus;
import org.dromara.warm.flow.core.enums.ActivityStatus;
import org.dromara.warm.flow.core.exception.FlowException;
import org.dromara.warm.flow.core.service.DefService;
import org.dromara.warm.flow.core.utils.StreamUtils;
import org.dromara.warm.flow.orm.entity.FlowDefinition;
import org.dromara.warm.flow.orm.entity.FlowHisTask;
import org.dromara.warm.flow.orm.entity.FlowNode;
import org.dromara.warm.flow.orm.entity.FlowSkip;
import org.dromara.warm.flow.orm.mapper.FlowDefinitionMapper;
import org.dromara.warm.flow.orm.mapper.FlowHisTaskMapper;
import org.dromara.warm.flow.orm.mapper.FlowNodeMapper;
import org.dromara.warm.flow.orm.mapper.FlowSkipMapper;
//import org.dromara.workflow.common.ConditionalOnEnable;
//import org.dromara.workflow.common.constant.FlowConstant;
//import org.dromara.workflow.domain.FlowCategory;
//import org.dromara.workflow.domain.vo.FlowDefinitionVo;
//import org.dromara.workflow.mapper.FlwCategoryMapper;
//import org.dromara.workflow.service.IFlwCommonService;
//import org.dromara.workflow.service.IFlwDefinitionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//import static org.dromara.common.core.constant.TenantConstants.DEFAULT_TENANT_ID;

/**
 * 流程定义 服务层实现
 *
 * @author may
 */
@ConditionalOnEnable
@Slf4j
@RequiredArgsConstructor
@Service
public class FlwDefinitionServiceImpl implements IFlwDefinitionService {

    private final DefService defService;
    private final FlowDefinitionMapper flowDefinitionMapper;
    private final FlowHisTaskMapper flowHisTaskMapper;
    private final FlowNodeMapper flowNodeMapper;
    private final FlowSkipMapper flowSkipMapper;
    private final FlwCategoryMapper flwCategoryMapper;
    private final IFlwCommonService flwCommonService;

    /**
     * 查询流程定义列表
     *
     * @param flowDefinition 流程定义信息
     * @param pageQuery      分页
     * @return 返回分页列表
     */
    @Override
    public Page<FlowDefinitionVo> queryList(FlowDefinition flowDefinition, PageQuery pageQuery) {
        LambdaQueryWrapper<FlowDefinition> wrapper = buildQueryWrapper(flowDefinition);
        wrapper.eq(FlowDefinition::getIsPublish, PublishStatus.PUBLISHED.getKey());
        onlyLatestDefinitionPerFlowCode(wrapper, PublishStatus.PUBLISHED.getKey());
        Page<FlowDefinition> page = flowDefinitionMapper.selectPage(pageQuery.build(), wrapper);
        return buildDefinitionVoPage(page);
    }

    /**
     * 查询未发布的流程定义列表
     *
     * @param flowDefinition 流程定义信息
     * @param pageQuery      分页
     * @return 返回分页列表
     */
    @Override
    public Page<FlowDefinitionVo> unPublishList(FlowDefinition flowDefinition, PageQuery pageQuery) {
        LambdaQueryWrapper<FlowDefinition> wrapper = buildQueryWrapper(flowDefinition);
        wrapper.in(FlowDefinition::getIsPublish, Arrays.asList(PublishStatus.UNPUBLISHED.getKey(), PublishStatus.EXPIRED.getKey()));
        onlyLatestDefinitionPerFlowCode(wrapper, PublishStatus.UNPUBLISHED.getKey(), PublishStatus.EXPIRED.getKey());
        Page<FlowDefinition> page = flowDefinitionMapper.selectPage(pageQuery.build(), wrapper);
        return buildDefinitionVoPage(page);
    }

    /**
     * 转换流程定义分页结果，补齐前端列表需要的分类名称等字段。
     */
    private Page<FlowDefinitionVo> buildDefinitionVoPage(Page<FlowDefinition> page) {
        Page<FlowDefinitionVo> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<FlowDefinitionVo> records = BeanUtil.copyToList(page.getRecords(), FlowDefinitionVo.class);
        if (CollUtil.isEmpty(records)) {
            result.setRecords(records);
            return result;
        }

        Map<String, String> categoryNameMap = new HashMap<>();
        List<Long> categoryIds = records.stream()
            .map(FlowDefinitionVo::getCategory)
            .filter(StringUtils::isNotBlank)
            .map(Convert::toLong)
            .distinct()
            .toList();
        if (CollUtil.isNotEmpty(categoryIds)) {
            flwCategoryMapper.selectByIds(categoryIds).forEach(category ->
                categoryNameMap.put(Convert.toStr(category.getCategoryId()), category.getCategoryName()));
        }

        records.forEach(item -> item.setCategoryName(categoryNameMap.get(item.getCategory())));
        result.setRecords(records);
        return result;
    }

    /**
     * A flow code keeps its historical versions for traceability, but the
     * definition list is a business list: show only the newest matching
     * version in each tab. This keeps pagination and totals correct without
     * deleting any history.
     */
    private void onlyLatestDefinitionPerFlowCode(LambdaQueryWrapper<FlowDefinition> wrapper, Integer... publishStatuses) {
        String statusList = Arrays.stream(publishStatuses)
            .map(String::valueOf)
            .collect(Collectors.joining(", "));
        wrapper.notExists("""
            select 1
            from flow_definition newer
            where newer.flow_code = flow_definition.flow_code
              and newer.del_flag = '0'
              and newer.is_publish in (%s)
              and coalesce(newer.tenant_id, '') = coalesce(flow_definition.tenant_id, '')
              and (
                newer.create_time > flow_definition.create_time
                or (newer.create_time = flow_definition.create_time and newer.id > flow_definition.id)
              )
            """.formatted(statusList));
    }

    private LambdaQueryWrapper<FlowDefinition> buildQueryWrapper(FlowDefinition flowDefinition) {
        LambdaQueryWrapper<FlowDefinition> wrapper = Wrappers.lambdaQuery();
        wrapper.like(StringUtils.isNotBlank(flowDefinition.getFlowCode()), FlowDefinition::getFlowCode, flowDefinition.getFlowCode());
        wrapper.like(StringUtils.isNotBlank(flowDefinition.getFlowName()), FlowDefinition::getFlowName, flowDefinition.getFlowName());
        if (StringUtils.isNotBlank(flowDefinition.getCategory())) {
            List<Long> categoryIds = flwCategoryMapper.selectCategoryIdsByParentId(Convert.toLong(flowDefinition.getCategory()));
            wrapper.in(FlowDefinition::getCategory, StreamUtils.toList(categoryIds, Convert::toStr));
        }
        wrapper.orderByDesc(FlowDefinition::getCreateTime);
        return wrapper;
    }

    /**
     * 发布流程定义
     *
     * @param id 流程定义id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publish(Long id) {
        FlowDefinition definition = flowDefinitionMapper.selectById(id);
        if (definition == null) {
            throw new BusinessException("流程定义不存在或已删除");
        }

        List<FlowNode> flowNodes = flowNodeMapper.selectList(
            new LambdaQueryWrapper<FlowNode>().eq(FlowNode::getDefinitionId, id)
        );
        if (CollUtil.isEmpty(flowNodes)) {
            throw new BusinessException("未绘制流程图，请先完成流程图设计并保存后再发布");
        }

        List<String> errorMsg = new ArrayList<>();
        String applyNodeCode = flwCommonService.applyNodeCode(id);
        for (FlowNode flowNode : flowNodes) {
            if (StringUtils.isBlank(flowNode.getPermissionFlag())
                && !applyNodeCode.equals(flowNode.getNodeCode())
                && NodeType.BETWEEN.getKey().equals(flowNode.getNodeType())) {
                errorMsg.add(flowNode.getNodeName());
            }
        }
        if (CollUtil.isNotEmpty(errorMsg)) {
            throw new BusinessException("节点【" + StringUtils.joinComma(errorMsg) + "】未配置办理人");
        }

        try {
            return defService.publish(id);
        } catch (FlowException e) {
            log.warn("流程定义发布校验失败, id={}, reason={}", id, e.getMessage());
            String message = StringUtils.isBlank(e.getMessage()) ? "流程发布校验失败，请检查流程图配置" : e.getMessage();
            throw new BusinessException(message);
        }
    }

    /**
     * 取消发布流程定义。取消后版本回到可设计、可删除的未发布状态。
     * 已产生过流程实例的版本必须保留发布历史，只能通过发布新版本使其失效。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unPublish(Long id) {
        FlowDefinition definition = flowDefinitionMapper.selectById(id);
        if (definition == null) {
            throw new BusinessException("流程定义不存在");
        }
        if (!PublishStatus.PUBLISHED.getKey().equals(definition.getIsPublish())) {
            throw new BusinessException("当前流程未发布，无需取消发布");
        }
        if (CollUtil.isNotEmpty(FlowEngine.insService().getByDefId(id))) {
            throw new BusinessException("流程已发起过实例，不能取消发布；请复制新版本并发布，历史版本会自动失效");
        }
        if (ActivityStatus.isActivity(definition.getActivityStatus())) {
            defService.unActive(id);
        }
        if (!defService.unPublish(id)) {
            throw new BusinessException("取消发布失败，请稍后重试");
        }
        return true;
    }

    /**
     * 导入流程定义
     *
     * @param file 文件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean importJson(MultipartFile file, String category) {
        try {
            DefJson defJson = JsonUtils.parseObject(file.getBytes(), DefJson.class);
            defJson.setCategory(category);
            defService.importDef(defJson);
        } catch (IOException e) {
            log.error("读取文件流错误: {}", e.getMessage(), e);
            throw new IllegalStateException("文件读取失败，请检查文件内容", e);
        }
        return true;
    }

    /**
     * 导出流程定义
     *
     * @param id       流程定义id
     * @param response 响应
     * @throws IOException 异常
     */
    @Override
    public void exportDef(Long id, HttpServletResponse response) throws IOException {
        byte[] data = defService.exportJson(id).getBytes(StandardCharsets.UTF_8);
        // 设置响应头和内容类型
        response.reset();
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/text");
        response.setHeader("Content-Disposition", "attachment;");
        response.addHeader("Content-Length", "" + data.length);
        IoUtil.write(response.getOutputStream(), false, data);
    }

    /**
     * 删除流程定义
     *
     * @param ids 流程定义id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeDef(List<Long> ids) {
        List<FlowDefinition> definitions = flowDefinitionMapper.selectByIds(ids);
        List<String> publishedCodes = StreamUtils.toList(
            definitions.stream()
                .filter(item -> PublishStatus.PUBLISHED.getKey().equals(item.getIsPublish()))
                .toList(),
            FlowDefinition::getFlowCode
        );
        if (CollUtil.isNotEmpty(publishedCodes)) {
            throw new BusinessException("流程定义【" + String.join("、", publishedCodes) + "】已发布，请先取消发布后再删除");
        }
        List<String> activeCodes = StreamUtils.toList(
            definitions.stream()
                .filter(item -> ActivityStatus.isActivity(item.getActivityStatus()))
                .toList(),
            FlowDefinition::getFlowCode
        );
        if (CollUtil.isNotEmpty(activeCodes)) {
            throw new BusinessException("流程定义【" + String.join("、", activeCodes) + "】已激活，请先挂起后再删除");
        }
        LambdaQueryWrapper<FlowHisTask> wrapper = Wrappers.lambdaQuery();
        wrapper.in(FlowHisTask::getDefinitionId, ids);
        List<FlowHisTask> flowHisTasks = flowHisTaskMapper.selectList(wrapper);
        if (CollUtil.isNotEmpty(flowHisTasks)) {
            List<FlowDefinition> flowDefinitions = flowDefinitionMapper.selectByIds(StreamUtils.toList(flowHisTasks, FlowHisTask::getDefinitionId));
            if (CollUtil.isNotEmpty(flowDefinitions)) {
                String join = StreamUtils.join(flowDefinitions, FlowDefinition::getFlowCode);
                log.info("流程定义【{}】已被使用不可被删除！", join);
                throw new BusinessException("流程定义【" + join + "】已被使用，不可删除");
            }
        }
        try {
            defService.removeDef(ids);
        } catch (Exception e) {
            log.error("Error removing flow definitions: {}", e.getMessage(), e);
            if (StringUtils.isNotBlank(e.getMessage()) && e.getMessage().contains("已开启过审批任务")) {
                throw new BusinessException("流程定义存在流程实例，已被使用，不可删除");
            }
            throw new BusinessException("删除流程定义失败，请稍后重试");
        }
        return true;
    }

    /**
     * 新增租户流程定义
     *
     * @param tenantId 租户id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncDef(String tenantId) {
        FlowCategory flowCategory = flwCategoryMapper.selectOne(new LambdaQueryWrapper<FlowCategory>()
            .eq(FlowCategory::getTenantId, "000000")
            .eq(FlowCategory::getCategoryId, FlowConstant.FLOW_CATEGORY_ID));
        flowCategory.setCategoryId(null);
        flowCategory.setTenantId(tenantId);
        flowCategory.setCreateDept(null);
        flowCategory.setCreateBy(null);
        flowCategory.setCreateTime(null);
        flowCategory.setUpdateBy(null);
        flowCategory.setUpdateTime(null);
        flwCategoryMapper.insert(flowCategory);

        List<FlowDefinition> flowDefinitions = flowDefinitionMapper.selectList(new LambdaQueryWrapper<FlowDefinition>().eq(FlowDefinition::getTenantId, "000000"));
        if (CollUtil.isEmpty(flowDefinitions)) {
            return;
        }
        List<Long> defIds = StreamUtils.toList(flowDefinitions, FlowDefinition::getId);
        List<FlowNode> flowNodes = flowNodeMapper.selectList(new LambdaQueryWrapper<FlowNode>().in(FlowNode::getDefinitionId, defIds));
        List<FlowSkip> flowSkips = flowSkipMapper.selectList(new LambdaQueryWrapper<FlowSkip>().in(FlowSkip::getDefinitionId, defIds));
        for (FlowDefinition definition : flowDefinitions) {
            FlowDefinition flowDefinition = BeanUtil.toBean(definition, FlowDefinition.class);
            flowDefinition.setId(null);
            flowDefinition.setTenantId(tenantId);
            flowDefinition.setIsPublish(0);
            flowDefinition.setCategory(Convert.toStr(flowCategory.getCategoryId()));
            int insert = flowDefinitionMapper.insert(flowDefinition);
            if (insert <= 0) {
                log.info("同步流程定义【{}】失败！", definition.getFlowCode());
                continue;
            }
            log.info("同步流程定义【{}】成功！", definition.getFlowCode());
            Long definitionId = flowDefinition.getId();
            if (CollUtil.isNotEmpty(flowNodes)) {
                List<FlowNode> nodes = StreamUtils.filter(flowNodes, node -> node.getDefinitionId().equals(definition.getId()));
                if (CollUtil.isNotEmpty(nodes)) {
                    List<FlowNode> flowNodeList = BeanUtil.copyToList(nodes, FlowNode.class);
                    flowNodeList.forEach(e -> {
                        e.setId(null);
                        e.setDefinitionId(definitionId);
                        e.setTenantId(tenantId);
                        e.setPermissionFlag(null);
                    });
                    flowNodeMapper.insertOrUpdate(flowNodeList);
                }
            }
            if (CollUtil.isNotEmpty(flowSkips)) {
                List<FlowSkip> skips = StreamUtils.filter(flowSkips, skip -> skip.getDefinitionId().equals(definition.getId()));
                if (CollUtil.isNotEmpty(skips)) {
                    List<FlowSkip> flowSkipList = BeanUtil.copyToList(skips, FlowSkip.class);
                    flowSkipList.forEach(e -> {
                        e.setId(null);
                        e.setDefinitionId(definitionId);
                        e.setTenantId(tenantId);
                    });
                    flowSkipMapper.insertOrUpdate(flowSkipList);
                }
            }
        }
    }
}
