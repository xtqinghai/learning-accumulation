package com.quinn.hboot.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quinn.hboot.system.model.entity.SysMenu;

import java.util.List;

/**
 * @author yu-qh
 */
public interface SysMenuService extends IService<SysMenu> {
    List<SysMenu> findMenusByUserId(Long userId);
}
