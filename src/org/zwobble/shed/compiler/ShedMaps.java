package org.zwobble.shed.compiler;

import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

public class ShedMaps {
    public static <K, V> Map<K, V> toMapWithKeys(Iterable<V> values, Function<V, K> keyGenerator) {
        ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        for (V value : values) {
            builder.put(keyGenerator.apply(value), value);
        }
        return builder.build();
    }
}
