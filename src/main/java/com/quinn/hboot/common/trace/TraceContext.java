package com.quinn.hboot.common.trace;

import org.slf4j.MDC;

import java.util.UUID;

public final class TraceContext {

    public static final String TRACE_HEADER = "X-Trace-Id";
    public static final String TRACE_ID_KEY = "traceId";
    public static final String BIZ_ID_KEY = "businessId";
    public static final String INSTANCE_ID_KEY = "instanceId";

    private TraceContext() {
    }

    public static String initTraceId(String traceId) {
        String current = normalize(traceId);
        if (current == null) {
            current = UUID.randomUUID().toString().replace("-", "");
        }
        MDC.put(TRACE_ID_KEY, current);
        return current;
    }

    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    public static void bindBusinessId(Object businessId) {
        putOrRemove(BIZ_ID_KEY, businessId);
    }

    public static void bindInstanceId(Object instanceId) {
        putOrRemove(INSTANCE_ID_KEY, instanceId);
    }

    public static String getBusinessId() {
        return MDC.get(BIZ_ID_KEY);
    }

    public static void clearBusinessId() {
        MDC.remove(BIZ_ID_KEY);
    }

    public static String getInstanceId() {
        return MDC.get(INSTANCE_ID_KEY);
    }

    public static void clearInstanceId() {
        MDC.remove(INSTANCE_ID_KEY);
    }

    public static void clearWorkflowContext() {
        clearBusinessId();
        clearInstanceId();
    }

    public static void clear() {
        MDC.remove(TRACE_ID_KEY);
        MDC.remove(BIZ_ID_KEY);
        MDC.remove(INSTANCE_ID_KEY);
    }

    private static void putOrRemove(String key, Object value) {
        String text = normalize(value);
        if (text == null) {
            MDC.remove(key);
            return;
        }
        MDC.put(key, text);
    }

    private static String normalize(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }
}
