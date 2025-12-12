package com.quinn.hboot.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quinn.hboot.system.mapper.SysDictItemMapper;
import com.quinn.hboot.system.model.entity.SysDictItem;
import com.quinn.hboot.system.service.SysDictItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author yu-qh
 */
@Service
@RequiredArgsConstructor
public class SysDictItemServiceImpl  extends ServiceImpl<SysDictItemMapper, SysDictItem>
        implements SysDictItemService {
}
