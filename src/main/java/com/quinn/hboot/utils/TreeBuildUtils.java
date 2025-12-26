package com.quinn.hboot.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @program: h-boot
 * @ClassName TreeUtils
 * @description: 说明：TODO
 * @author: yuqh
 * @create: 2025-12-13 09:18
 * @Version 1.0
 **/
public class TreeBuildUtils {
    // 后端工具方法：扁平菜单列表转树形
    /**
     * 通用树形结构构建方法
     *
     * @param list           扁平的节点列表
     * @param idGetter       获取节点ID的函数
     * @param parentIdGetter 获取节点父ID的函数
     * @param childrenSetter 设置节点子节点的函数
     * @param childrenGetter 获取节点子节点的函数
     * @param rootParentId   根节点的父ID值（通常为0）
     * @param <T>            节点类型
     * @param <ID>           ID类型（Long/Integer等）
     * @return 构建好的树形结构列表
     */
    public static <T, ID> List<T> buildTree(
            List<T> list,
            Function<T, ID> idGetter,
            Function<T, ID> parentIdGetter,
            java.util.function.BiConsumer<T, List<T>> childrenSetter,
            Function<T, List<T>> childrenGetter,
            ID rootParentId) {

        // 空值校验
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 构建ID->节点的映射，方便快速查找
        Map<ID, T> nodeMap = list.stream()
                .collect(Collectors.toMap(idGetter, node -> node));

        // 2. 组装树形结构
        List<T> tree = new ArrayList<>();
        for (T node : list) {
            ID parentId = parentIdGetter.apply(node);
            // 判断是否为根节点
            if (rootParentId.equals(parentId)) {
                tree.add(node);
            } else {
                // 找到父节点并添加子节点
                T parentNode = nodeMap.get(parentId);
                if (parentNode != null) {
                    List<T> children = childrenGetter.apply(parentNode);
                    if (children == null) {
                        children = new ArrayList<>();
                        childrenSetter.accept(parentNode, children);
                    }
                    children.add(node);
                }
            }
        }

        return tree;
    }
}
