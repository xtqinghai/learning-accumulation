package com.quinn.hboot.flow.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.kotlin.KtQueryChainWrapper;
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quinn.hboot.flow.mapper.WarmFlowMapper;
import com.quinn.hboot.flow.service.ExecuteService;
import com.quinn.hboot.flow.vo.FlowTaskVo;
import com.quinn.hboot.flow.vo.WarmFlowInteractiveTypeVo;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.dromara.warm.flow.core.entity.HisTask;
import org.dromara.warm.flow.core.entity.Task;
import org.dromara.warm.flow.core.entity.User;
import org.dromara.warm.flow.orm.entity.FlowHisTask;
import org.dromara.warm.flow.orm.entity.FlowTask;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

/**
 * 流程执行SERVICEIMPL
 *
 * @author WARM
 * @since 2023/5/29 13:09
 */
@Service
@RequiredArgsConstructor
public class ExecuteServiceImpl  extends ServiceImpl<WarmFlowMapper, FlowTaskVo> implements ExecuteService {

    @Resource
    private WarmFlowMapper flowMapper;

//    @Autowired
//    private UserMapper userMapper;

//    @Autowired
//    private DeptMapper deptMapper;

//    @Autowired
//    private RoleMapper roleMapper;

    @Override
    public List<FlowTaskVo> toDoPage(Task task) {
        return flowMapper.toDoPage(task);
    }

    @Override
    public List<FlowHisTask> donePage(HisTask hisTask) {
        return flowMapper.donePage(hisTask);
    }

    @Override
    public List<FlowHisTask> copyPage(FlowTask flowTask) {
        return flowMapper.copyPage(flowTask);
    }

    /**
     * 根据ID反显姓名
     *
     * @param ids 需要反显姓名的用户ID
     * @return {@link List<User>}
     * @author liangli
     * @date 2024/8/21 17:11
     **/
    @Override
    public List<User> idReverseDisplayName(Long[] ids) {
        if (Objects.isNull(ids) || ids.length == 0) {
            return null;
        }
        return flowMapper.idReverseDisplayName(ids);
    }

    /**
     * 查询不包含输入的所有用户
     *
     * @param warmFlowInteractiveTypeVo 输入用户编号集合
     * @return 用户列表
     */
    @Override
    public List<User> selectNotUserList(WarmFlowInteractiveTypeVo warmFlowInteractiveTypeVo) {
        return flowMapper.selectNotUserIds(warmFlowInteractiveTypeVo);
    }

    /**
     * 查询包含输入的所有用户
     *
     * @param warmFlowInteractiveTypeVo 输入用户编号集合
     * @return 用户列表
     */
    @Override
    public List<User> selectUserList(WarmFlowInteractiveTypeVo warmFlowInteractiveTypeVo) {
        return flowMapper.selectUserIds(warmFlowInteractiveTypeVo);
    }

    /**
     * 根据ID查询名称
     */
    @Override
    public String getName(String id) {
        /*Map<Long, String> userMap = StreamUtils.toMap(userMapper.selectUserList(new User())
                , User::getUserId, User::getNickName);
        Map<Long, String> deptMap = StreamUtils.toMap(deptMapper.selectDeptList(new SysDept())
                , SysDept::getDeptId, SysDept::getDeptName);
        Map<Long, String> roleMap = StreamUtils.toMap(roleMapper.selectRoleAll()
                , SysRole::getRoleId, SysRole::getRoleName);
        if (StringUtils.isNotNull(id)) {
            if (id.contains("user:")) {
                String name = userMap.get(Long.valueOf(id.replace("user:", "")));
                if (StringUtils.isNotEmpty(name)) {
                    return "用户:" + name;
                }
            } else if (id.contains("dept:")) {
                String name = deptMap.get(Long.valueOf(id.replace("dept:", "")));
                if (StringUtils.isNotEmpty(name)) {
                    return "部门:" + name;
                }
            } else if (id.contains("role")) {
                String name = roleMap.get(Long.valueOf(id.replace("role:", "")));
                if (StringUtils.isNotEmpty(name)) {
                    return "角色:" + name;
                }
            } else {
                try {
                    long parseLong = Long.parseLong(id);
                    String name = userMap.get(parseLong);
                    if (StringUtils.isNotEmpty(name)) {
                        return "用户:" + name;
                    }
                } catch (NumberFormatException e) {
                    return id;
                }

            }
        }
        return "";*/
        return id;
    }

    /**
     * 根据ID查询名称
     */
    @Override
    public Map<String, String> getNameMap(List<String> ids) {
        /*Map<Long, String> userMap = StreamUtils.toMap(userMapper.selectUserList(new User())
                , User::getUserId, User::getNickName);
        Map<Long, String> deptMap = StreamUtils.toMap(deptMapper.selectDeptList(new SysDept())
                , SysDept::getDeptId, SysDept::getDeptName);
        Map<Long, String> roleMap = StreamUtils.toMap(roleMapper.selectRoleAll()
                , SysRole::getRoleId, SysRole::getRoleName);
        Map<String, String> map = new HashMap<>();
        if (CollUtil.isNotEmpty(ids)) {
            for (String id : ids) {
                if (id.contains("user:")) {
                    String name = userMap.get(Long.valueOf(id.replace("user:", "")));
                    if (StringUtils.isNotEmpty(name)) {
                        map.put("用户", name);
                    }
                } else if (id.contains("dept:")) {
                    String name = deptMap.get(Long.valueOf(id.replace("dept:", "")));
                    if (StringUtils.isNotEmpty(name)) {
                        map.put("部门", name);
                    }
                } else if (id.contains("role")) {
                    String name = roleMap.get(Long.valueOf(id.replace("role:", "")));
                    if (StringUtils.isNotEmpty(name)) {
                        map.put("角色", name);
                    }
                } else {
                    try {
                        long parseLong = Long.parseLong(id);
                        String name = userMap.get(parseLong);
                        if (StringUtils.isNotEmpty(name)) {
                            map.put("用户", name);
                        }
                    } catch (NumberFormatException e) {
                        return MapUtil.newAndPut(id, id);
                    }

                }
            }
        }
        return map;*/
        return new HashMap<>();
    }

    @Override
    public boolean save(FlowTaskVo entity) {
        return super.save(entity);
    }

    @Override
    public boolean saveBatch(Collection<FlowTaskVo> entityList) {
        return super.saveBatch(entityList);
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<FlowTaskVo> entityList) {
        return super.saveOrUpdateBatch(entityList);
    }

    @Override
    public boolean removeById(FlowTaskVo entity) {
        return super.removeById(entity);
    }

    @Override
    public boolean removeByMap(Map<String, Object> columnMap) {
        return super.removeByMap(columnMap);
    }

    @Override
    public boolean remove(Wrapper<FlowTaskVo> queryWrapper) {
        return super.remove(queryWrapper);
    }

    @Override
    public boolean removeByIds(Collection<?> list, boolean useFill) {
        return super.removeByIds(list, useFill);
    }

    @Override
    public boolean removeBatchByIds(Collection<?> list) {
        return super.removeBatchByIds(list);
    }

    @Override
    public boolean removeBatchByIds(Collection<?> list, boolean useFill) {
        return super.removeBatchByIds(list, useFill);
    }

    @Override
    public boolean updateById(FlowTaskVo entity) {
        return super.updateById(entity);
    }

    @Override
    public boolean update(Wrapper<FlowTaskVo> updateWrapper) {
        return super.update(updateWrapper);
    }

    @Override
    public boolean update(FlowTaskVo entity, Wrapper<FlowTaskVo> updateWrapper) {
        return super.update(entity, updateWrapper);
    }

    @Override
    public boolean updateBatchById(Collection<FlowTaskVo> entityList) {
        return super.updateBatchById(entityList);
    }

    @Override
    public FlowTaskVo getById(Serializable id) {
        return super.getById(id);
    }

    @Override
    public Optional<FlowTaskVo> getOptById(Serializable id) {
        return super.getOptById(id);
    }

    @Override
    public List<FlowTaskVo> listByIds(Collection<? extends Serializable> idList) {
        return super.listByIds(idList);
    }

    @Override
    public List<FlowTaskVo> listByMap(Map<String, Object> columnMap) {
        return super.listByMap(columnMap);
    }

    @Override
    public FlowTaskVo getOne(Wrapper<FlowTaskVo> queryWrapper) {
        return super.getOne(queryWrapper);
    }

    @Override
    public Optional<FlowTaskVo> getOneOpt(Wrapper<FlowTaskVo> queryWrapper) {
        return super.getOneOpt(queryWrapper);
    }

    @Override
    public boolean exists(Wrapper<FlowTaskVo> queryWrapper) {
        return super.exists(queryWrapper);
    }


    @Override
    public long count() {
        return super.count();
    }

    @Override
    public long count(Wrapper<FlowTaskVo> queryWrapper) {
        return super.count(queryWrapper);
    }

    @Override
    public List<FlowTaskVo> list(Wrapper<FlowTaskVo> queryWrapper) {
        return super.list(queryWrapper);
    }

    @Override
    public List<FlowTaskVo> list(IPage<FlowTaskVo> page, Wrapper<FlowTaskVo> queryWrapper) {
        return super.list(page, queryWrapper);
    }

    @Override
    public List<FlowTaskVo> list() {
        return super.list();
    }

    @Override
    public List<FlowTaskVo> list(IPage<FlowTaskVo> page) {
        return super.list(page);
    }

    @Override
    public <E extends IPage<FlowTaskVo>> E page(E page, Wrapper<FlowTaskVo> queryWrapper) {
        return super.page(page, queryWrapper);
    }

    @Override
    public <E extends IPage<FlowTaskVo>> E page(E page) {
        return super.page(page);
    }

    @Override
    public List<Map<String, Object>> listMaps(Wrapper<FlowTaskVo> queryWrapper) {
        return super.listMaps(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> listMaps(IPage<? extends Map<String, Object>> page, Wrapper<FlowTaskVo> queryWrapper) {
        return super.listMaps(page, queryWrapper);
    }

    @Override
    public List<Map<String, Object>> listMaps() {
        return super.listMaps();
    }

    @Override
    public List<Map<String, Object>> listMaps(IPage<? extends Map<String, Object>> page) {
        return super.listMaps(page);
    }

    @Override
    public <E> List<E> listObjs() {
        return super.listObjs();
    }

    @Override
    public <V> List<V> listObjs(Function<? super Object, V> mapper) {
        return super.listObjs(mapper);
    }

    @Override
    public <E> List<E> listObjs(Wrapper<FlowTaskVo> queryWrapper) {
        return super.listObjs(queryWrapper);
    }

    @Override
    public <V> List<V> listObjs(Wrapper<FlowTaskVo> queryWrapper, Function<? super Object, V> mapper) {
        return super.listObjs(queryWrapper, mapper);
    }

    @Override
    public <E extends IPage<Map<String, Object>>> E pageMaps(E page, Wrapper<FlowTaskVo> queryWrapper) {
        return super.pageMaps(page, queryWrapper);
    }

    @Override
    public <E extends IPage<Map<String, Object>>> E pageMaps(E page) {
        return super.pageMaps(page);
    }

    @Override
    public QueryChainWrapper<FlowTaskVo> query() {
        return super.query();
    }

    @Override
    public LambdaQueryChainWrapper<FlowTaskVo> lambdaQuery() {
        return super.lambdaQuery();
    }

    @Override
    public LambdaQueryChainWrapper<FlowTaskVo> lambdaQuery(FlowTaskVo entity) {
        return super.lambdaQuery(entity);
    }

    @Override
    public KtQueryChainWrapper<FlowTaskVo> ktQuery() {
        return super.ktQuery();
    }

    @Override
    public KtUpdateChainWrapper<FlowTaskVo> ktUpdate() {
        return super.ktUpdate();
    }

    @Override
    public UpdateChainWrapper<FlowTaskVo> update() {
        return super.update();
    }

    @Override
    public LambdaUpdateChainWrapper<FlowTaskVo> lambdaUpdate() {
        return super.lambdaUpdate();
    }

    @Override
    public boolean saveOrUpdate(FlowTaskVo entity, Wrapper<FlowTaskVo> updateWrapper) {
        return super.saveOrUpdate(entity, updateWrapper);
    }


}
