package com.quinn.hboot.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quinn.hboot.system.mapper.SysDeptMapper;
import com.quinn.hboot.system.mapper.SysMenuMapper;
import com.quinn.hboot.system.mapper.SysUserMapper;
import com.quinn.hboot.system.model.dto.RegisterUserDTO;
import com.quinn.hboot.system.model.entity.SysDept;
import com.quinn.hboot.system.model.entity.SysMenu;
import com.quinn.hboot.system.model.entity.SysUser;
import com.quinn.hboot.system.model.entity.SysUserRole;
import com.quinn.hboot.system.service.SysMenuService;
import com.quinn.hboot.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 * @author yu-qh
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService{

    private final SysMenuService menuService;
//    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void register(RegisterUserDTO dto) {

        // 校验 orgId 是否存在
        SysDept org = deptMapper.selectById(dto.getOrgId());
        if (org == null) {
            throw new RuntimeException("机构不存在");
        }

        // 校验用户名是否已存在
        Long count = this.getBaseMapper().selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, dto.getUsername())
        );
        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 构建用户
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setNickname(dto.getNickname());
        user.setDeptId(dto.getOrgId());
        user.setStatus(1);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        // 保存
        this.getBaseMapper().insert(user);
    }

    @Override
    public SysUser getByUsername(String username) {
        return this.getBaseMapper().selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        List<SysMenu> menus = menuService.findMenusByUserId(userId);
        return menus.stream()
                .map(SysMenu::getPermission)
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }

}
