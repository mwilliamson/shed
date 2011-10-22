package org.zwobble.shed.compiler.referenceresolution;

import java.util.Map;

import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration;

public class TopScope implements Scope {
    private final Map<String, GlobalDeclaration> globalDeclarations;
    
    public TopScope(Map<String, GlobalDeclaration> globalDeclarations) {
        this.globalDeclarations = globalDeclarations;
    }
    
    @Override
    public Result lookup(String identifier) {
        if (globalDeclarations.containsKey(identifier)) {
            return new Success(globalDeclarations.get(identifier));
        } else {
            return new NotInScope();
        }
    }
}
