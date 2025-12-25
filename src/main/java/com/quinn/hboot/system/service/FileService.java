package com.quinn.hboot.system.service;

import com.quinn.hboot.system.model.dto.FileChunkUploadDTO;
import com.quinn.hboot.system.model.entity.SysFile;
import com.quinn.hboot.system.model.form.FileForm;
import com.quinn.hboot.system.model.query.FileQuery;
import com.quinn.hboot.system.model.vo.FileVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 文件管理服务类
 *
 * @author yu-qh
 * @since 2025-12-25 22:55
 */
public interface FileService extends IService<SysFile> {

    /**
     *文件管理分页列表
     *
     * @return {@link IPage<FileVO>} 文件管理分页列表
     */
    IPage<FileVO> getFilePage(FileQuery queryParams);

    /**
     * 获取文件管理表单数据
     *
     * @param id 文件管理ID
     * @return 文件管理表单数据
     */
     FileForm getFileFormData(Long id);

    /**
     * 新增文件管理
     *
     * @param formData 文件管理表单对象
     * @return 是否新增成功
     */
    boolean saveFile(FileForm formData);

    /**
     * 修改文件管理
     *
     * @param id   文件管理ID
     * @param formData 文件管理表单对象
     * @return 是否修改成功
     */
    boolean updateFile(Long id, FileForm formData);

    /**
     * 删除文件管理
     *
     * @param ids 文件管理ID，多个以英文逗号(,)分割
     * @return 是否删除成功
     */
    boolean deleteFiles(String ids);

    SysFile upload(MultipartFile file) throws Exception;

    void download(Long id, HttpServletResponse response) throws Exception;

    void preview(Long id, HttpServletResponse response) throws Exception;

    void delete(Long id);

    void deleteBatch(List<Long> ids);

    void uploadChunk(MultipartFile file, FileChunkUploadDTO dto)  throws IOException;

    SysFile mergeChunks(FileChunkUploadDTO dto)  throws IOException ;

    /**
     * 保存文件
     */
    String save(InputStream inputStream, String path, String contentType) throws IOException;

    /**
     * 获取文件流
     */
    InputStream get(String path) throws IOException;

    /**
     * 删除文件
     */
    void delete(String path) throws IOException;
}
