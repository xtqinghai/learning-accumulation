package com.quinn.hboot.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quinn.hboot.system.model.entity.SysDictItem;

import java.util.List;

public interface SysDictItemService extends IService<SysDictItem> {

    List<SysDictItem> listByDictType(String dictType);
}
