package com.quinn.hboot.system.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * 文件存储适配器。业务层只依赖这个接口，后续可替换为对象存储实现。
 */
public interface FileStorageAdapter {

    String save(InputStream inputStream, String path, String contentType) throws IOException;

    InputStream get(String path) throws IOException;

    void delete(String path) throws IOException;

    long size(String path) throws IOException;

    Path resolve(String path);
}
