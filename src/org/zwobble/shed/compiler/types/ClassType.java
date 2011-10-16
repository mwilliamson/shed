package org.zwobble.shed.compiler.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import org.zwobble.shed.compiler.naming.FullyQualifiedName;

@ToString
@AllArgsConstructor
@Getter
public class ClassType implements ScalarType {
    private final FullyQualifiedName fullyQualifiedName;
    
    @Override
    public String shortName() {
        return fullyQualifiedName.last();
    }
}
