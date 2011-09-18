package org.zwobble.shed.compiler.referenceresolution;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Identity;

public class SubScope implements Scope {
    private final Scope parentScope;
    private final Map<String, Identity<DeclarationNode>> variables = new HashMap<String, Identity<DeclarationNode>>();
    private final Set<String> allVariablesDeclaredInScope;

    public SubScope(Scope parentScope, Set<String> allVariablesDeclaredInScope) {
        this.parentScope = parentScope;
        this.allVariablesDeclaredInScope = allVariablesDeclaredInScope;
    }
    
    public void add(String identifier, DeclarationNode node) {
        variables.put(identifier, new Identity<DeclarationNode>(node));
    }

    @Override
    public Result lookup(String identifier) {
        if (variables.containsKey(identifier)) {
            return new Success(variables.get(identifier).get()); 
        } else if (allVariablesDeclaredInScope.contains(identifier)) {
            return new NotDeclaredYet();
        } else {
            return parentScope.lookup(identifier);
        }
    }

    public boolean isDeclaredInCurrentScope(String identifier) {
        return variables.containsKey(identifier);
    }
}
