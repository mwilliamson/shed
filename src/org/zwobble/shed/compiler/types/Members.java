package org.zwobble.shed.compiler.types;

import java.util.Map;

import org.zwobble.shed.compiler.typechecker.ValueInfo;

import com.google.common.collect.ImmutableMap;

public class Members {
    public static MembersBuilder builder() {
        return new MembersBuilder();
    }
    
    public static Map<String, ValueInfo> members() {
        return ImmutableMap.of();
    }
    
    public static Map<String, ValueInfo> members(String name1, ValueInfo value1) {
        return ImmutableMap.of(name1, value1);
    }
    
    public static Map<String, ValueInfo> members(String name1, ValueInfo value1, String name2, ValueInfo value2) {
        return ImmutableMap.of(name1, value1, name2, value2);
    }
}
