package com.quinn.hboot.utils;

/**
 * @author yu-qh
 */
import com.quinn.hboot.system.model.entity.SysMenu;

import java.util.*;
import java.util.stream.Collectors;

public class MenuTreeUtil {

    public static List<SysMenu> buildTree(List<SysMenu> list) {
        Map<Long, SysMenu> map = list.stream().collect(Collectors.toMap(SysMenu::getId, m -> m));
        List<SysMenu> roots = new ArrayList<>();
        for (SysMenu m : list) {
            Long pid = m.getParentId();
            if (pid == null || pid == 0) {
                roots.add(m);
            } else {
                SysMenu parent = map.get(pid);
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(m);
                } else {
                    roots.add(m); // orphan => treat as root
                }
            }
        }
        // optional: sort children by sort field if needed
        return roots;
    }
}
