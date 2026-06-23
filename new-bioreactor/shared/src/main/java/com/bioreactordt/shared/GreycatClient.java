package com.bioreactordt.shared;

import greycat.GreyCat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class GreycatClient {

    @Value("${greycat.url}")
    private String url;

    private GreyCat greycat;


    private synchronized GreyCat ensureConnected() throws Exception {
        if (greycat == null) {
            greycat = new GreyCat(url, null, false, false);
            log.info("GreyCat connected at {}", url);
        }
        return greycat;
    }


//    public Object call(String fn, Object... args) throws Exception {
//        GreyCat gc = ensureConnected();
//        try {
//            return gc.call(fn, args);
//        } catch (Exception e) {
//            String msg = e.getMessage();
//            if (msg != null && (msg.contains("connect") || msg.contains("refused") || msg.contains("timeout"))) {
//                synchronized (this) { greycat = null; }
//            }
//            throw new RuntimeException("GreyCat [" + fn + "]: " + msg, e);
//        }
//    }

    public synchronized Object call(String fn, Object... args) throws Exception {
        GreyCat gc = ensureConnected();
        try {
            return gc.call(fn, args);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && (msg.contains("connect") || msg.contains("refused") || msg.contains("timeout"))) {
                greycat = null;
            }
            throw new RuntimeException("GreyCat [" + fn + "]: " + msg, e);
        }
    }


/*
    public Object call(String fn, Object... args) {
        try {
            if  (greycat == null) connect();
            return greycat.call(fn, args);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && (msg.contains("connect") || msg.contains("refused") || msg.contains("timeout"))) {
                greycat = null;
            }
            throw new RuntimeException("GreyCat [" + fn + "]: " + msg, e);
        }
    }
*/
    @SuppressWarnings("unchecked")
    public Map<String, Object> callMap(String fn, Object... args) throws Exception {
        Object converted = toJava(call(fn, args));
        if (converted == null) return null;

        if (converted instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }

        if (converted instanceof List<?> list) {
            if (list.isEmpty()) return null;
            return (Map<String, Object>) list.get(0);
        }

        throw new RuntimeException("GreyCat [" + fn + "]: unexpected return type " + converted.getClass());
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> callList(String fn, Object... args) throws Exception {
        Object converted = toJava(call(fn, args));
        if (converted == null) return List.of();

        if (converted instanceof List<?> list) {
            return list.stream()
                    .filter(java.util.Objects::nonNull)
                    .map(o -> (Map<String, Object>) o)
                    .toList();
        }

        if (converted instanceof Map<?, ?> map) {
            return List.of((Map<String, Object>) map);
        }

        throw new RuntimeException("GreyCat [" + fn + "]: unexpected return type " + converted.getClass());
    }


    private Object toJava(Object raw) {
        if (raw == null) return null;

        if (raw instanceof Map<?, ?> || raw instanceof List<?> || raw instanceof Object[]
                || raw instanceof String || raw instanceof Number || raw instanceof Boolean) {
            if (raw instanceof Object[] arr) {
                List<Object> result = new ArrayList<>(arr.length);
                for (Object o : arr) result.add(toJava(o));
                return result;
            }
            if (raw instanceof List<?> list) {
                List<Object> result = new ArrayList<>(list.size());
                for (Object o : list) result.add(toJava(o));
                return result;
            }
            if (raw instanceof Map<?, ?> map) {
                Map<Object, Object> result = new LinkedHashMap<>();
                for (Map.Entry<?, ?> e : map.entrySet()) {
                    result.put(e.getKey(), toJava(e.getValue()));
                }
                return result;
            }
            return raw;
        }

        Class<?> cls = raw.getClass();
        String name = cls.getName();

        try {
            // greycat.gc$core$Array (or any GreyCat array-like wrapper) -> java.util.List
            if (name.contains("core$Array") || name.contains("core.Array")) {
                List<?> list = (List<?>) cls.getMethod("toList").invoke(raw);
                List<Object> result = new ArrayList<>(list.size());
                for (Object o : list) result.add(toJava(o));
                return result;
            }

            // greycat.gc$core$Map (or any GreyCat map-like wrapper) -> java.util.Map
            if (name.contains("core$Map") || name.contains("core.Map")) {
                Map<Object, Object> result = new LinkedHashMap<>();
                Set<?> keys = (Set<?>) cls.getMethod("keys").invoke(raw);
                for (Object k : keys) {
                    Object v = cls.getMethod("get", Object.class).invoke(raw, k);
                    result.put(k, toJava(v));
                }
                return result;
            }
        } catch (Exception e) {
            throw new RuntimeException("GreyCat: failed to convert result of type " + name, e);
        }
        return raw;
    }


}