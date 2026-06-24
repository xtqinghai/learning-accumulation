package com.quinn.hboot.utils;

import com.quinn.hboot.system.model.entity.SysMenu;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MenuTreeUtilTest {

    @Test
    void shouldBuildTreeWithoutDuplicatingChildrenOnRepeatedCalls() {
        SysMenu root = menu(1L, 0L, "system");
        SysMenu child = menu(2L, 1L, "user");
        SysMenu duplicateChild = menu(2L, 1L, "user");

        List<SysMenu> first = MenuTreeUtil.buildTree(List.of(root, child, duplicateChild));
        List<SysMenu> second = MenuTreeUtil.buildTree(List.of(root, child, duplicateChild));

        assertEquals(1, first.size());
        assertEquals(1, second.size());
        assertNotNull(first.get(0).getChildren());
        assertNotNull(second.get(0).getChildren());
        assertEquals(1, first.get(0).getChildren().size());
        assertEquals(1, second.get(0).getChildren().size());
        assertEquals(2L, second.get(0).getChildren().get(0).getId());
    }

    @Test
    void shouldTreatOrphanMenuAsRoot() {
        SysMenu orphan = menu(3L, 99L, "orphan");

        List<SysMenu> tree = MenuTreeUtil.buildTree(List.of(orphan));

        assertEquals(1, tree.size());
        assertEquals(3L, tree.get(0).getId());
    }

    private SysMenu menu(Long id, Long parentId, String name) {
        SysMenu menu = new SysMenu();
        menu.setId(id);
        menu.setParentId(parentId);
        menu.setName(name);
        menu.setPath("/" + name);
        menu.setType(1);
        menu.setSort(0);
        menu.setStatus(1);
        return menu;
    }
}
