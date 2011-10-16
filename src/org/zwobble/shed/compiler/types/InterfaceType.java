package org.zwobble.shed.compiler.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import org.zwobble.shed.compiler.naming.FullyQualifiedName;

@AllArgsConstructor
@ToString
@Getter
public class InterfaceType implements ScalarType {
    private final FullyQualifiedName fullyQualifiedName;
    
    @Override
    public String shortName() {
        return fullyQualifiedName.last();
    }
}
