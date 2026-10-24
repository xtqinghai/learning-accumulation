package com.quinn.hboot.system.service.impl;

import com.quinn.hboot.common.exception.BusinessException;
import com.quinn.hboot.system.converter.CmsContentConverterImpl;
import com.quinn.hboot.system.mapper.CmsContentMapper;
import com.quinn.hboot.system.model.entity.CmsContent;
import com.quinn.hboot.system.model.form.CmsContentForm;
import com.quinn.hboot.utils.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CmsContentServiceImplTest {

    @Test
    void saveShouldPassContentAndTopToMapper() {
        CmsContentMapper mapper = mock(CmsContentMapper.class);
        when(mapper.insert(any(CmsContent.class))).thenReturn(1);
        CmsContentServiceImpl service = new CmsContentServiceImpl(new CmsContentConverterImpl());
        ReflectionTestUtils.setField(service, "baseMapper", mapper);

        CmsContentForm form = validForm();
        form.setContent("<p>保存后的正文</p>");
        form.setTop(1);

        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getUsername).thenReturn("tester");

            assertTrue(service.saveCmsContent(form));
            verify(mapper).insert(org.mockito.ArgumentMatchers.<CmsContent>argThat(entity ->
                "<p>保存后的正文</p>".equals(entity.getContent())
                    && Integer.valueOf(1).equals(entity.getTop())
                    && "tester".equals(entity.getCreator())
            ));
        }
    }

    @Test
    void saveShouldRejectBlankContent() {
        CmsContentMapper mapper = mock(CmsContentMapper.class);
        CmsContentServiceImpl service = new CmsContentServiceImpl(new CmsContentConverterImpl());
        ReflectionTestUtils.setField(service, "baseMapper", mapper);

        CmsContentForm form = validForm();
        form.setContent(" ");

        assertThrows(BusinessException.class, () -> service.saveCmsContent(form));
    }

    private CmsContentForm validForm() {
        CmsContentForm form = new CmsContentForm();
        form.setType("Update_Log");
        form.setTitle("测试内容");
        form.setSummary("测试摘要");
        form.setStatus(1);
        form.setSort(0);
        form.setTop(0);
        return form;
    }
}
