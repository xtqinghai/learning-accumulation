package com.quinn.hboot.flow.controller;

import com.quinn.hboot.common.R;
import com.quinn.hboot.flow.vo.TableDataInfo;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.formula.functions.T;
import org.dromara.warm.flow.core.entity.Definition;
import org.dromara.warm.flow.core.service.ChartService;
import org.dromara.warm.flow.core.service.DefService;
import org.dromara.warm.flow.core.utils.page.Page;
import org.dromara.warm.flow.orm.entity.FlowDefinition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 流程定义Controller
 *
 * @author hh
 * @date 2023-04-11
 */
//@Validated
@RestController
@RequestMapping("/api/flow/definition")
@RequiredArgsConstructor
public class DefController {
    @Resource
    private DefService defService;

    @Resource
    private ChartService chartService;

    /**
     * 分页查询流程定义列表
     */
    @GetMapping("/list")
    public R list(FlowDefinition flowDefinition, int pageNum , int pageSize) {
        // flow组件自带分页功能
//        PageDomain pageDomain = TableSupport.buildPageRequest();
        Page<Definition> page = Page.pageOf(pageNum, pageSize);
        page = defService.orderByCreateTime().desc().page(null, page);
//        TableDataInfo rspData = new TableDataInfo();
//        rspData.setCode("00000");
//        rspData.setMsg("查询成功");
//        List<Definition> list = page.getList();
//        rspData.setData(new TableDataInfo.Data(list,page.getTotal()));
        return R.ok(page);
    }


    /**
     * 获取流程定义详细信息
     */
//    @PreAuthorize("@ss.hasPermi('flow:definition:query')")
    @GetMapping(value = "/{id}")
    public R<Definition> getInfo(@PathVariable("id") Long id) {
        return R.ok(defService.getById(id));
    }

    /**
     * 新增流程定义
     */
//    @PreAuthorize("@ss.hasPermi('flow:definition:add')")
//    @Log(value = "流程定义", module = LogModuleEnum.FLOW)
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> add(@RequestBody FlowDefinition flowDefinition) {
        return R.ok(defService.checkAndSave(flowDefinition));
    }

    /**
     * 发布流程定义
     */
//    @PreAuthorize("@ss.hasPermi('flow:definition:publish')")
//    @Log(value = "流程定义", module = LogModuleEnum.FLOW)
    @GetMapping("/publish/{id}")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> publish(@PathVariable("id") Long id) {
        return R.ok(defService.publish(id));
    }

    /**
     * 取消发布流程定义
     */
//    @PreAuthorize("@ss.hasPermi('flow:definition:publish')")
//    @Log(value = "流程定义", module = LogModuleEnum.FLOW)
    @GetMapping("/unPublish/{id}")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> unPublish(@PathVariable("id") Long id) {
        defService.unPublish(id);
        return R.ok();
    }

    /**
     * 修改流程定义
     */
//    @PreAuthorize("@ss.hasPermi('flow:definition:edit')")
//    @Log(value = "流程定义", module = LogModuleEnum.FLOW)
    @PutMapping
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> edit(@RequestBody FlowDefinition flowDefinition) {
        return R.ok(defService.updateById(flowDefinition));
    }

    /**
     * 删除流程定义
     */
//    @PreAuthorize("@ss.hasPermi('flow:definition:remove')")
//    @Log(value = "流程定义", module = LogModuleEnum.FLOW)
    @DeleteMapping("/{ids}")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> remove(@PathVariable List<Long> ids) {
        return R.ok(defService.removeDef(ids));
    }

    /**
     * 复制流程定义
     */
//    @PreAuthorize("@ss.hasPermi('flow:definition:publish')")
//    @Log(value = "流程定义-copyDef", module = LogModuleEnum.FLOW)
    @GetMapping("/copyDef/{id}")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> copyDef(@PathVariable("id") Long id) {
        return R.ok(defService.copyDef(id));
    }

//    @Log(value = "流程定义", module = LogModuleEnum.FLOW)
//    @PreAuthorize("@ss.hasPermi('flow:definition:importDefinition')")
    @PostMapping("/importDefinition")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> importDefinition(MultipartFile file) throws Exception {
        defService.importIs(file.getInputStream());
        return R.ok();
    }

//    @Log(value = "流程定义-exportDefinition", module = LogModuleEnum.FLOW)
//    @PreAuthorize("@ss.hasPermi('flow:definition:exportDefinition')")
    @PostMapping("/exportDefinition/{id}")
    public ResponseEntity<byte[]> exportDefinition(@PathVariable("id") Long id) {
        // 要导出的字符串
        String content = defService.exportJson(id);

        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=exported_string.txt");

        // 返回响应
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.TEXT_PLAIN)
                .body(content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 激活流程
     *
     * @param definitionId
     * @return
     */
    @GetMapping("/active/{definitionId}")
    public R<Boolean> active(@PathVariable("definitionId") Long definitionId) {
        return R.ok(defService.active(definitionId));
    }

    /**
     * 挂起流程
     *
     * @param definitionId
     * @return
     */
    @GetMapping("/unActive/{definitionId}")
    public R<Boolean> unActive(@PathVariable("definitionId") Long definitionId) {
        return R.ok(defService.unActive(definitionId));
    }
}
