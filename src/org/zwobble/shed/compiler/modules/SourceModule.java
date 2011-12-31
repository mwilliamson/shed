package org.zwobble.shed.compiler.modules;

import lombok.Data;

import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.parsing.nodes.Declaration;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;

@Data(staticConstructor="create")
public class SourceModule implements Module {
    private final SourceNode source;
    
    @Override
    public Declaration getDeclaration() {
        return source.getPublicDeclaration().get();
    }
    
    @Override
    public FullyQualifiedName getName() {
        return source.name();
    }
}
