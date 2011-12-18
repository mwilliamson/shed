package org.zwobble.shed.compiler.util;

import java.util.Map;

import org.zwobble.shed.compiler.Option;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.Option.some;

public class ShedMaps {
    public static <K, V> Map<K, V> toMapWithKeys(Iterable<V> values, Function<V, K> keyGenerator) {
        ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        for (V value : values) {
            builder.put(keyGenerator.apply(value), value);
        }
        return builder.build();
    }

    public static <K, V> Option<V> getOrNone(Map<K, V> map, K key) {
        if (map.containsKey(key)) {
            return some(map.get(key));
        } else {
            return none();
        }
    }
}
