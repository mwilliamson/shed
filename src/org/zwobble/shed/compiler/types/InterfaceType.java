package org.zwobble.shed.compiler.types;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import lombok.Data;

import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.typechecker.ValueInfo;

@Data
public class InterfaceType implements ScalarType {
    private final FullyQualifiedName fullyQualifiedName;
    private final Map<String, ValueInfo> members;
    
    @Override
    public String shortName() {
        return fullyQualifiedName.last();
    }
    
    @Override
    public Set<InterfaceType> superTypes() {
        return Collections.emptySet();
    }
}
