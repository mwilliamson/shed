package org.zwobble.shed.compiler.referenceresolution;

import java.util.Map;

import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclarationNode;

public class TopScope implements Scope {
    private final Map<String, GlobalDeclarationNode> globalDeclarations;
    
    public TopScope(Map<String, GlobalDeclarationNode> globalDeclarations) {
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
