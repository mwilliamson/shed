package org.zwobble.shed.compiler.types;

import org.zwobble.shed.compiler.typechecker.ValueInfo;

import com.google.common.collect.ImmutableMap;

public class MembersBuilder {
    private final ImmutableMap.Builder<String, ValueInfo> members = ImmutableMap.builder();
    
    public void add(String name, ValueInfo valueInfo) {
        members.put(name, valueInfo);
    }
    
    public Members build() {
        return Members.members(members.build());
    }
}
