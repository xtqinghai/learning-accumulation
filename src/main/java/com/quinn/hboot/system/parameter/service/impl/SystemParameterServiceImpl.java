package com.quinn.hboot.system.parameter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quinn.hboot.common.exception.BusinessException;
import com.quinn.hboot.system.parameter.mapper.SystemParameterMapper;
import com.quinn.hboot.system.parameter.model.entity.SystemParameter;
import com.quinn.hboot.system.parameter.model.form.SystemParameterForm;
import com.quinn.hboot.system.parameter.model.query.SystemParameterQuery;
import com.quinn.hboot.system.parameter.model.vo.SystemParameterVO;
import com.quinn.hboot.system.parameter.service.SystemParameterService;
import com.quinn.hboot.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class SystemParameterServiceImpl implements SystemParameterService {

    private static final int DISABLED = 0;
    private static final int SENSITIVE = 1;
    private static final String ENCRYPTED_PREFIX = "enc:v1:";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SystemParameterMapper parameterMapper;

    @Value("${system.parameter.encryption-key:}")
    private String encryptionKey;

    @Override
    public IPage<SystemParameterVO> page(SystemParameterQuery query) {
        Page<SystemParameter> page = parameterMapper.selectPage(query.buildPage(), new LambdaQueryWrapper<SystemParameter>()
            .like(StringUtils.hasText(query.getParameterKey()), SystemParameter::getParameterKey, query.getParameterKey())
            .eq(StringUtils.hasText(query.getCategory()), SystemParameter::getCategory, query.getCategory())
            .eq(query.getStatus() != null, SystemParameter::getStatus, query.getStatus())
            .orderByAsc(SystemParameter::getCategory)
            .orderByAsc(SystemParameter::getParameterKey));
        return page.convert(this::toVO);
    }

    @Override
    public SystemParameterVO get(Long id) {
        return toVO(require(id));
    }

    @Override
    public Long create(SystemParameterForm form) {
        validateUniqueKey(form.getParameterKey(), null);
        if (form.getSensitive() == SENSITIVE && form.getParameterValue() == null) {
            throw new BusinessException("敏感参数必须提供参数值");
        }
        SystemParameter entity = new SystemParameter();
        copy(form, entity, false);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreator(SecurityUtils.getUsername());
        entity.setUpdater(SecurityUtils.getUsername());
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        parameterMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, SystemParameterForm form) {
        SystemParameter entity = require(id);
        validateUniqueKey(form.getParameterKey(), id);
        copy(form, entity, true);
        entity.setUpdater(SecurityUtils.getUsername());
        entity.setUpdateTime(LocalDateTime.now());
        parameterMapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        require(id);
        parameterMapper.deleteById(id);
    }

    @Override
    public String getEnabledValue(String parameterKey) {
        SystemParameter parameter = parameterMapper.selectOne(new LambdaQueryWrapper<SystemParameter>()
            .eq(SystemParameter::getParameterKey, parameterKey)
            .eq(SystemParameter::getStatus, 1));
        if (parameter == null) {
            return null;
        }
        return parameter.getSensitive() == SENSITIVE ? decrypt(parameter.getParameterValue()) : parameter.getParameterValue();
    }

    private SystemParameter require(Long id) {
        SystemParameter entity = parameterMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("系统参数不存在");
        }
        return entity;
    }

    private void validateUniqueKey(String parameterKey, Long currentId) {
        Long count = parameterMapper.selectCount(new LambdaQueryWrapper<SystemParameter>()
            .eq(SystemParameter::getParameterKey, parameterKey.trim())
            .ne(currentId != null, SystemParameter::getId, currentId));
        if (count != null && count > 0) {
            throw new BusinessException("参数键已存在");
        }
    }

    private void copy(SystemParameterForm form, SystemParameter entity, boolean updating) {
        entity.setParameterKey(form.getParameterKey().trim());
        entity.setParameterName(form.getParameterName().trim());
        entity.setCategory(form.getCategory().trim());
        entity.setSensitive(form.getSensitive());
        entity.setStatus(form.getStatus());
        entity.setRemark(StringUtils.hasText(form.getRemark()) ? form.getRemark().trim() : null);

        if (updating && form.getSensitive() == SENSITIVE && form.getParameterValue() == null) {
            if (entity.getSensitive() != SENSITIVE) {
                throw new BusinessException("将参数标记为敏感时必须提供参数值");
            }
            return;
        }
        if (updating && entity.getSensitive() == SENSITIVE && form.getSensitive() != SENSITIVE
            && form.getParameterValue() == null) {
            throw new BusinessException("将敏感参数改为普通参数时必须重新提供参数值");
        }
        String value = form.getParameterValue() == null ? "" : form.getParameterValue();
        entity.setParameterValue(form.getSensitive() == SENSITIVE ? encrypt(value) : value);
    }

    private SystemParameterVO toVO(SystemParameter entity) {
        SystemParameterVO result = new SystemParameterVO();
        result.setId(entity.getId());
        result.setParameterKey(entity.getParameterKey());
        result.setParameterName(entity.getParameterName());
        result.setParameterValue(entity.getSensitive() == SENSITIVE ? "******" : entity.getParameterValue());
        result.setCategory(entity.getCategory());
        result.setSensitive(entity.getSensitive());
        result.setStatus(entity.getStatus());
        result.setRemark(entity.getRemark());
        result.setCreator(entity.getCreator());
        result.setCreateTime(entity.getCreateTime());
        result.setUpdater(entity.getUpdater());
        result.setUpdateTime(entity.getUpdateTime());
        return result;
    }

    private String encrypt(String plainText) {
        try {
            byte[] nonce = new byte[12];
            RANDOM.nextBytes(nonce);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(128, nonce));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return ENCRYPTED_PREFIX + Base64.getEncoder().encodeToString(nonce) + ":"
                + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception exception) {
            throw new BusinessException("敏感参数加密失败");
        }
    }

    private String decrypt(String encryptedValue) {
        try {
            if (!StringUtils.hasText(encryptedValue) || !encryptedValue.startsWith(ENCRYPTED_PREFIX)) {
                throw new BusinessException("敏感参数密文格式无效");
            }
            String[] values = encryptedValue.substring(ENCRYPTED_PREFIX.length()).split(":", -1);
            if (values.length != 2) {
                throw new BusinessException("敏感参数密文格式无效");
            }
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(128, Base64.getDecoder().decode(values[0])));
            return new String(cipher.doFinal(Base64.getDecoder().decode(values[1])), StandardCharsets.UTF_8);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException("敏感参数解密失败");
        }
    }

    private SecretKeySpec key() {
        byte[] keyBytes = encryptionKey == null ? new byte[0] : encryptionKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new BusinessException("敏感参数需要配置 16、24 或 32 字节的 APP_SYSTEM_PARAMETER_ENCRYPTION_KEY");
        }
        return new SecretKeySpec(keyBytes, "AES");
    }
}
