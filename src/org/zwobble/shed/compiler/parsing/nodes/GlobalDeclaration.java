package org.zwobble.shed.compiler.parsing.nodes;

import org.zwobble.shed.compiler.naming.FullyQualifiedName;

import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;

import lombok.Data;

@Data(staticConstructor="globalDeclaration")
public class GlobalDeclaration implements Declaration {
    public static GlobalDeclaration globalDeclaration(String... names) {
        return globalDeclaration(fullyQualifiedName(names));
    }
    
    private final FullyQualifiedName name;
    
    @Override
    public String getIdentifier() {
        return name.last();
    }
}
