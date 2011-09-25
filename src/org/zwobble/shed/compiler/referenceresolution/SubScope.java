package org.zwobble.shed.compiler.referenceresolution;

import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;

public class SubScope implements Scope {
    private final Scope parentScope;
    private final DeclarationIdentifierMap variables;

    public SubScope(Scope parentScope) {
        this(parentScope, DeclarationIdentifierMap.empty());
    }
    
    private SubScope(Scope parentScope, DeclarationIdentifierMap variables) {
        this.parentScope = parentScope;
        this.variables = variables;
    }
    
    public void add(String identifier, DeclarationNode node) {
        variables.put(identifier, node);            
    }

    @Override
    public Result lookup(String identifier) {
        if (variables.isDeclared(identifier)) {
            return new Success(variables.get(identifier)); 
        } else {
            return parentScope.lookup(identifier);
        }
    }

    public boolean isDeclaredInCurrentScope(String identifier) {
        return variables.isDeclared(identifier);
    }
    
    public SubScope extend() {
        return new SubScope(parentScope, variables.extend());
    }
}
