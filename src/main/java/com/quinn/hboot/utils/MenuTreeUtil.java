package com.quinn.hboot.utils;

/**
 * @author yu-qh
 */
import com.quinn.hboot.system.model.entity.SysMenu;

import java.util.*;
import java.util.stream.Collectors;

public class MenuTreeUtil {

    public static List<SysMenu> buildTree(List<SysMenu> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, SysMenu> map = new LinkedHashMap<>();
        for (SysMenu menu : list) {
            if (menu == null || menu.getId() == null) {
                continue;
            }
            menu.setChildren(null);
            map.putIfAbsent(menu.getId(), menu);
        }

        List<SysMenu> roots = new ArrayList<>();
        for (SysMenu m : map.values()) {
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
        return roots;
    }
}
