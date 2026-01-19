package com.quinn.hboot.module.flow.controller;

import com.quinn.hboot.common.R;
import com.quinn.hboot.flow.vo.TableDataInfo;
import com.quinn.hboot.module.flow.domain.TestLeave;
import com.quinn.hboot.module.flow.service.ITestLeaveService;
import jakarta.servlet.http.HttpServletResponse;
import org.dromara.warm.flow.core.utils.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * OA 请假申请Controller
 *
 * @author ruoyi
 * @date 2024-03-07
 */
@RestController
@RequestMapping("/system/leave")
public class TestLeaveController
{
    @Autowired
    private ITestLeaveService testLeaveService;

    /**
     * 查询OA 请假申请列表
     */
    @PreAuthorize("@ss.hasPermi('system:leave:list')")
    @GetMapping("/list")
    public TableDataInfo list(TestLeave testLeave)
    {
//        startPage();
        List<TestLeave> list = testLeaveService.selectTestLeaveList(testLeave);
        return getDataTable(list,list.size());
    }

    /**
     * 导出OA 请假申请列表
     */
//    @PreAuthorize("@ss.hasPermi('system:leave:export')")
//    @Log(title = "OA 请假申请", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TestLeave testLeave)
    {
//        List<TestLeave> list = testLeaveService.selectTestLeaveList(testLeave);
//        ExcelUtil<TestLeave> util = new ExcelUtil<TestLeave>(TestLeave.class);
//        util.exportExcel(response, list, "OA 请假申请数据");
    }

    /**
     * 获取OA 请假申请详细信息
     */
    @GetMapping(value = "/{id}")
    public R getInfo(@PathVariable("id") String id)
    {
        return R.ok(testLeaveService.selectTestLeaveById(id));
    }

    /**
     * 新增OA 请假申请
     */
//    @PreAuthorize("@ss.hasPermi('system:leave:add')")
//    @Log(title = "OA 请假申请", businessType = BusinessType.INSERT)
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public R add(@RequestBody TestLeave testLeave, String flowStatus)
    {
        return R.ok(testLeaveService.insertTestLeave(testLeave, flowStatus));
    }

    /**
     * 修改OA 请假申请
     */
//    @PreAuthorize("@ss.hasPermi('system:leave:edit')")
//    @Log(title = "OA 请假申请", businessType = BusinessType.UPDATE)
    @PutMapping
    @Transactional(rollbackFor = Exception.class)
    public R edit(@RequestBody TestLeave testLeave)
    {
        return R.ok(testLeaveService.updateTestLeave(testLeave));
    }

    /**
     * 删除OA 请假申请
     */
//    @PreAuthorize("@ss.hasPermi('system:leave:remove')")
//    @Log(title = "OA 请假申请", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    @Transactional(rollbackFor = Exception.class)
    public R remove(@PathVariable String[] ids)
    {
        return R.ok(testLeaveService.deleteTestLeaveByIds(ids));
    }

    /**
     * 提交审批
     */
//    @PreAuthorize("@ss.hasPermi('system:leave:submit')")
//    @Log(title = "OA 请假申请", businessType = BusinessType.OTHER)
    @GetMapping(value = "/submit")
    @Transactional(rollbackFor = Exception.class)
    public R submit(String id, String flowStatus) {
        return R.ok(testLeaveService.submit(id, flowStatus));
    }

    /**
     * 办理
     */
//    @Log(title = "流程实例", businessType = BusinessType.OTHER)
    @PostMapping("/handle")
    @Transactional(rollbackFor = Exception.class)
    public R handle(@RequestBody TestLeave testLeave, Long taskId, String skipType, String message
            , String nodeCode, String flowStatus) {
        return R.ok(testLeaveService.handle(testLeave, taskId, skipType, message, nodeCode, flowStatus));
    }

    /**
     * 驳回上一个任务
     */
//    @Log(title = "流程实例", businessType = BusinessType.OTHER)
    @PostMapping("/rejectLast")
    @Transactional(rollbackFor = Exception.class)
    public R rejectLast(@RequestBody TestLeave testLeave, Long taskId, String message
            , String flowStatus) {
        return R.ok(testLeaveService.rejectLast(testLeave, taskId, message, flowStatus));
    }

    /**
     * 拿回到最近办理的任务
     */
//    @Log(title = "流程实例", businessType = BusinessType.OTHER)
    @PostMapping("/taskBack")
    @Transactional(rollbackFor = Exception.class)
    public R taskBack(@RequestBody TestLeave testLeave, Long taskId, String message
            , String flowStatus) {
        return R.ok(testLeaveService.taskBack(testLeave, taskId, message, flowStatus));
    }

    /**
     * 撤销流程
     */
//    @Log(title = "流程实例", businessType = BusinessType.OTHER)
    @GetMapping("/revoke/{id}")
    @Transactional(rollbackFor = Exception.class)
    public R revoke(@PathVariable String id) {
        return R.ok(testLeaveService.revoke(id));
    }

    /**
     * 拿回到最近办理的任务
     */
//    @Log(title = "流程实例", businessType = BusinessType.OTHER)
    @GetMapping("/taskBackByInsId/{id}")
    @Transactional(rollbackFor = Exception.class)
    public R taskBackByInsId(@PathVariable String id) {
        return R.ok(testLeaveService.taskBackByInsId(id));
    }

    /**
     * 终止流程，提前结束
     *
     * @param testLeave
     * @return
     */
    @PostMapping("/termination")
    @Transactional(rollbackFor = Exception.class)
    public R termination(@RequestBody TestLeave testLeave) {
        return R.ok(testLeaveService.termination(testLeave));
    }
    protected TableDataInfo getDataTable(List<?> list,long total)
    {
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(200);
        rspData.setMsg("查询成功");
        rspData.setRecord(new TableDataInfo.Data(list,total));
        return rspData;
    }
}
