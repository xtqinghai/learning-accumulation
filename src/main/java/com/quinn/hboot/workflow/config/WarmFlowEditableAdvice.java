package com.quinn.hboot.workflow.config;

import com.quinn.hboot.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.dromara.warm.flow.core.dto.ApiResult;
import org.dromara.warm.flow.core.dto.DefJson;
import org.dromara.warm.flow.core.enums.ActivityStatus;
import org.dromara.warm.flow.orm.entity.FlowDefinition;
import org.dromara.warm.flow.orm.mapper.FlowDefinitionMapper;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Type;

/**
 * Warm-Flow 1.8.9 treats a published definition as read-only in its UI.
 *
 * <p>The application's business rule is different: an inactive definition may
 * still be designed, while an activated definition must be read-only. The
 * designer shim marks inactive design requests with this header, allowing the
 * returned definition to be edited without changing the persisted publish
 * state shown by the application.</p>
 */
@ControllerAdvice
@RequiredArgsConstructor
public class WarmFlowEditableAdvice implements ResponseBodyAdvice<Object>, RequestBodyAdvice {

    private static final String EDITABLE_HEADER = "X-WarmFlow-Editable";
    private static final String SAVE_JSON_PATH = "/warm-flow/save-json";

    private final FlowDefinitionMapper flowDefinitionMapper;

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        String path = request.getURI().getPath();
        if (!path.startsWith("/warm-flow/query-def")) {
            return body;
        }
        if (body instanceof ApiResult<?> result && result.getData() instanceof DefJson definition) {
            // Warm-Flow 1.8.9 does not copy the definition id in
            // DefJson.copyDef(Definition). Without it the designer treats an
            // existing definition as a new one when saving its nodes.
            Long definitionId = resolveDefinitionId(path);
            if (definitionId != null) {
                definition.setId(definitionId);
            }
            if ("true".equalsIgnoreCase(request.getHeaders().getFirst(EDITABLE_HEADER))) {
                // The flow remains published in the database; this only unlocks
                // the 1.8.9 designer for an inactive definition.
                definition.setIsPublish(0);
            }
        }
        return body;
    }

    private Long resolveDefinitionId(String path) {
        String prefix = "/warm-flow/query-def/";
        if (!path.startsWith(prefix)) {
            return null;
        }
        String value = path.substring(prefix.length());
        if (value.isBlank() || value.contains("/")) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @Override
    public boolean supports(MethodParameter methodParameter,
                            Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        // The external 1.8.9 controller declares DefJson directly, but some
        // Spring/Jackson combinations present the resolved type in a form that
        // fails the old isAssignableFrom check. afterBodyRead still verifies
        // the actual body type, so accepting all read requests is safe here.
        return true;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage,
                                           MethodParameter parameter,
                                           Type targetType,
                                           Class<? extends HttpMessageConverter<?>> converterType) {
        return inputMessage;
    }

    @Override
    public Object afterBodyRead(Object body,
                                HttpInputMessage inputMessage,
                                MethodParameter parameter,
                                Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {
        ServletRequestAttributes attributes =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest servletRequest = attributes == null ? null : attributes.getRequest();
        boolean saveJsonRequest = servletRequest != null
            && SAVE_JSON_PATH.equals(servletRequest.getRequestURI());
        if (saveJsonRequest && body instanceof DefJson definitionRequest && definitionRequest.getId() != null) {
            FlowDefinition current = flowDefinitionMapper.selectById(definitionRequest.getId());
            if (current != null && ActivityStatus.isActivity(current.getActivityStatus())) {
                throw new BusinessException("流程已激活，请先挂起后再设计");
            }
        }
        return body;
    }

    @Override
    public Object handleEmptyBody(Object body,
                                  HttpInputMessage inputMessage,
                                  MethodParameter parameter,
                                  Type targetType,
                                  Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }
}
