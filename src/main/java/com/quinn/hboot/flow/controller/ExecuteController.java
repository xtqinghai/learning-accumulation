package com.quinn.hboot.flow.controller;

import com.quinn.hboot.common.R;
import com.quinn.hboot.flow.service.ExecuteService;
import com.quinn.hboot.flow.service.HhDefService;
import com.quinn.hboot.flow.vo.FlowTaskVo;
import com.quinn.hboot.flow.vo.TableDataInfo;
import com.quinn.hboot.flow.vo.WarmFlowInteractiveTypeVo;
import com.quinn.hboot.system.model.vo.LoginUser;
import com.quinn.hboot.utils.SecurityUtils;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dromara.warm.flow.core.entity.*;
import org.dromara.warm.flow.core.enums.CooperateType;
import org.dromara.warm.flow.core.enums.UserType;
import org.dromara.warm.flow.core.service.*;
import org.dromara.warm.flow.core.utils.StreamUtils;
import org.dromara.warm.flow.orm.entity.FlowHisTask;
import org.dromara.warm.flow.orm.entity.FlowTask;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**load
 * 流程实例Controller
 *
 * @author hh
 * @date 2023-04-18
 */
//@Validated
@RestController
@RequestMapping("/api/flow/execute")
@RequiredArgsConstructor
public class ExecuteController {

//    @Autowired
//    private UserMapper userMapper;

//    @Autowired
//    private DeptMapper deptMapper;

//    @Autowired
//    private RoleMapper roleMapper;

    @Resource
    private HisTaskService hisTaskService;

    @Resource
    private TaskService taskService;

    @Resource
    private NodeService nodeService;

    @Resource
    private InsService insService;

    @Resource
    private UserService flowUserservice;

    @Resource
    private ExecuteService executeService;

    @Resource
    private HhDefService hhDefService;

    /**
     * 分页待办任务列表
     */
//    @PreAuthorize("@ss.hasPermi('flow:execute:toDoPage')")
    @GetMapping("/toDoPage")
    public TableDataInfo toDoPage(FlowTask flowTask) {
        //List<String> permissionList = permissionList(String.valueOf(SecurityUtils.getUserId()), SecurityUtils.getDeptId(), sysUser);
//        flowTask.setPermissionList(permissionList);
//        startPage();
        List<FlowTaskVo> list = executeService.toDoPage(flowTask);
        List<Long> taskIds = StreamUtils.toList(list, FlowTaskVo::getId);
        List<User> userList = flowUserservice.getByAssociateds(taskIds);
        Map<Long, List<User>> map = StreamUtils.groupByKey(userList, User::getAssociated);
        for (FlowTaskVo taskVo : list) {
            if (taskVo!=null) {
                List<User> users = map.get(taskVo.getId());
                if (CollectionUtils.isNotEmpty(users)) {
                    for (User user : users) {
                        if (UserType.APPROVAL.getKey().equals(user.getType())) {
                            if (StringUtils.isEmpty(taskVo.getApprover())) {
                                taskVo.setApprover("");
                            }
                            String name = executeService.getName(user.getProcessedBy());
                            if (StringUtils.isNotEmpty(name)) {
                                taskVo.setApprover(taskVo.getApprover().concat(name).concat(";"));
                            }
                        } else if (UserType.TRANSFER.getKey().equals(user.getType())) {
                            if (StringUtils.isEmpty(taskVo.getTransferredBy())) {
                                taskVo.setTransferredBy("");
                            }
                            String name = executeService.getName(user.getProcessedBy());
                            if (StringUtils.isNotEmpty(name)) {
                                taskVo.setTransferredBy(taskVo.getTransferredBy().concat(name).concat(";"));
                            }
                        } else if (UserType.DEPUTE.getKey().equals(user.getType())) {
                            if (StringUtils.isEmpty(taskVo.getDelegate())) {
                                taskVo.setDelegate("");
                            }
                            String name = executeService.getName(user.getProcessedBy());
                            if (StringUtils.isNotEmpty(name)) {
                                taskVo.setDelegate(taskVo.getDelegate().concat(name).concat(";"));
                            }
                        }
                    }
                }
            }
        }
        return getDataTable(list,list.size());
    }

    /**
     * 分页抄送任务列表
     * author：暗影
     */
//    @PreAuthorize("@ss.hasPermi('flow:execute:copyPage')")
    @GetMapping("/copyPage")
    public TableDataInfo copyPage(FlowTask flowTask) {
        LoginUser sysUser = SecurityUtils.getLoginUser();
        List<String> permissionList = permissionList(String.valueOf(sysUser.getUsername()), sysUser.getOrgId(), null);
        flowTask.setPermissionList(permissionList);
//        startPage();
        List<FlowHisTask> list = executeService.copyPage(flowTask);
        return getDataTable(list,list.size());
    }
    /**
     * 分页已办任务列表
     */
//    @PreAuthorize("@ss.hasPermi('flow:execute:donePage')")
    @GetMapping("/donePage")
    public TableDataInfo donePage(FlowHisTask flowHisTask) {
//        startPage();
//        User sysUser = SecurityUtils.getUser();
        flowHisTask.setApprover(SecurityUtils.getUserId().toString());
        List<FlowHisTask> list = executeService.donePage(flowHisTask);
        /*Map<Long, String> userMap = StreamUtils.toMap(userMapper.selectUserList(new User())
                , User::getUserId, User::getNickName);
        if (CollectionUtils.isNotEmpty(list)) {
            for (FlowHisTask hisTask : list) {
                if (StringUtils.isNotEmpty(hisTask.getApprover())) {
                    String name = executeService.getName(hisTask.getApprover());
                    hisTask.setApprover(name);
                }
                if (StringUtils.isNotEmpty(hisTask.getCollaborator())) {
                    String[] split = hisTask.getCollaborator().split(",");
                    if (ArrayUtils.isNotEmpty(split)) {
                        List<String> names = new ArrayList<>();
                        for (String s : split) {
                            names.add(userMap.get(Long.valueOf(s)));
                        }
                        hisTask.setCollaborator(StringUtils.join(names, ","));
                    }
                }
            }
        }*/
        return getDataTable(list,list.size());
    }


    /**
     * 查询已办任务历史记录
     */
    @PreAuthorize("@ss.hasPermi('flow:execute:doneList')")
    @GetMapping("/doneList/{instanceId}")
    public R<List<HisTask>> doneList(@PathVariable("instanceId") Long instanceId) {
        List<HisTask> flowHisTasks = hisTaskService.orderById().desc().list(new FlowHisTask().setInstanceId(instanceId));
        /*Map<Long, String> userMap = StreamUtils.toMap(userMapper.selectUserList(new User())
                , User::getUserId, User::getNickName);
        List<FlowHisTask> flowHisTaskList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(flowHisTasks)) {
            for (HisTask hisTask : flowHisTasks) {
                if (StringUtils.isNotEmpty(hisTask.getApprover())) {
                    String name = executeService.getName(hisTask.getApprover());
                    hisTask.setApprover(name);
                }
                if (StringUtils.isNotEmpty(hisTask.getCollaborator())) {
                    String[] split = hisTask.getCollaborator().split(",");
                    if (ArrayUtils.isNotEmpty(split)) {
                        List<String> names = new ArrayList<>();
                        for (String s : split) {
                            names.add(userMap.get(Long.valueOf(s)));
                        }
                        hisTask.setCollaborator(StringUtils.join(names, ","));
                    }
                }
                FlowHisTask flowHisTask = new FlowHisTask();
                BeanUtils.copyProperties(hisTask, flowHisTask);
                flowHisTaskList.add(flowHisTask);
            }
        }*/
//        return R.ok(flowHisTaskList);
        return R.ok(flowHisTasks);
    }

    /**
     * 根据taskId查询代表任务
     *
     * @param taskId
     * @return
     */
    @GetMapping("/getTaskById/{taskId}")
    public R<Task> getTaskById(@PathVariable("taskId") Long taskId) {
        return R.ok(taskService.getById(taskId));
    }

    /**
     * 查询跳转任意节点列表
     */
//    @PreAuthorize("@ss.hasPermi('flow:execute:doneList')")
    @GetMapping("/anyNodeList/{instanceId}")
    public R<List<Node>> anyNodeList(@PathVariable("instanceId") Long instanceId) {
        Instance instance = insService.getById(instanceId);
        Node startNode = nodeService.getStartNode(instance.getDefinitionId());
        List<Node> nodeList = nodeService.suffixNodeList(startNode.getId());
        return R.ok(nodeList);
    }

    /**
     * 处理非办理的流程交互类型
     *
     * @param warmFlowInteractiveTypeVo 要转办用户
     * @return 是否成功
     */
    @PostMapping("/interactiveType")
    public R<Boolean> interactiveType(WarmFlowInteractiveTypeVo warmFlowInteractiveTypeVo) {
        return R.ok(hhDefService.interactiveType(warmFlowInteractiveTypeVo));
    }

    /**
     * 交互类型可以选择的用户
     *
     * @param warmFlowInteractiveTypeVo 交互类型请求类
     * @return 是否成功
     */
    @GetMapping("/interactiveTypeSysUser")
    public TableDataInfo interactiveTypeSysUser(WarmFlowInteractiveTypeVo warmFlowInteractiveTypeVo) {
//        startPage();
//        User currentUser = SecurityUtils.getUser();
        Long userId = SecurityUtils.getUserId();
        Integer operatorType = warmFlowInteractiveTypeVo.getOperatorType();
        List<User> list;
        Long taskId = warmFlowInteractiveTypeVo.getTaskId();
        List<User> users = flowUserservice.listByAssociatedAndTypes(taskId);
        if (!Objects.equals(CooperateType.REDUCTION_SIGNATURE.getKey(), operatorType)) {
            List<String> userIds = StreamUtils.toList(users, User::getProcessedBy);
            warmFlowInteractiveTypeVo.setUserIds(userIds);
//            startPage();
            list = executeService.selectNotUserList(warmFlowInteractiveTypeVo);
        } else {
            List<String> userIds = StreamUtils.toList(users, User::getProcessedBy);
            warmFlowInteractiveTypeVo.setUserIds(userIds);
//            startPage();
            list = executeService.selectUserList(warmFlowInteractiveTypeVo);
            list = StreamUtils.filter(list, sysUser -> !Objects.equals(userId, SecurityUtils.getUserId()));
        }
        return getDataTable(list,list.size());
    }

    /**
     * 激活流程
     *
     * @param instanceId
     * @return
     */
    @GetMapping("/active/{instanceId}")
    public R<Boolean> active(@PathVariable("instanceId") Long instanceId) {
        return R.ok(insService.active(instanceId));
    }

    /**
     * 挂起流程
     *
     * @param instanceId
     * @return
     */
    @GetMapping("/unActive/{instanceId}")
    public R<Boolean> unActive(@PathVariable("instanceId") Long instanceId) {
        return R.ok(insService.unActive(instanceId));
    }

    /**
     * 获取权限
     *
     * @param userId  用户编号
     * @param deptId  部门编号
     * @param sysUser 登陆用户
     * @return 权限列表
     */
    private List<String> permissionList(String userId, Long deptId, User sysUser) {
//        List<Role> roles = sysUser.getRoles();
        List<String> permissionList = new ArrayList<>();
        /*if (CollectionUtils.isNotEmpty(roles)) {
            permissionList = StreamUtils.toList(roles, role -> "role:" + role.getRoleId());
        }
        permissionList.add(userId);
        if (Objects.nonNull(deptId)) {
            permissionList.add("dept:" + deptId);
        }
        logger.info("当前用户所有权限[{}]", permissionList);*/
        return permissionList;
    }

    /**
     * 根据ID反显姓名
     *
     * @param ids 需要反显姓名的用户ID
     * @return {@link R< List<User>>}
     * @author liangli
     * @date 2024/8/21 17:08
     **/
//    @PreAuthorize("@ss.hasPermi('flow:definition:query')")
    @GetMapping(value = "/idReverseDisplayName/{ids}")
    public R<List<User>> idReverseDisplayName(@PathVariable Long[] ids) {
        return R.ok(executeService.idReverseDisplayName(ids));
    }
    protected TableDataInfo getDataTable(List<?> list,long total)
    {
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(200);
        rspData.setMsg("查询成功");
        rspData.setRecord(new TableDataInfo.Data<>(list,total));
        return rspData;
    }
}
