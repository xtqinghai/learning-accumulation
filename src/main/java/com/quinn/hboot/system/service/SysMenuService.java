package com.quinn.hboot.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quinn.hboot.system.model.entity.SysMenu;

import java.util.List;

public interface SysMenuService extends IService<SysMenu> {

    List<SysMenu> findMenusByUserId(Long userId);

    List<SysMenu> findMenuTreeByUserId(Long userId);

    List<String> findPermissionsByUserId(Long userId);

    List<SysMenu> listVisibleMenus();

    List<SysMenu> listVisibleMenuTree();
}
