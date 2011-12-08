package org.zwobble.shed.compiler;

import java.util.Map;

import com.google.common.collect.Maps;

public class CompilationData {
    private final Map<CompilationDataKey<?>, Object> data = Maps.newHashMap();
    
    @SuppressWarnings("unchecked")
    public <T> T get(CompilationDataKey<T> key) {
        return (T)data.get(key);
    }

    public <T> void add(CompilationDataKey<T> key, T value) {
        data.put(key, value);
    }

    public <T> void addAll(CompilationData other) {
        data.putAll(other.data);
    }
}
