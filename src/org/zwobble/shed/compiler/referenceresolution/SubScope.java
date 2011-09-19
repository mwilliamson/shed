package org.zwobble.shed.compiler.referenceresolution;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Identity;

public class SubScope implements Scope {
    private final Scope parentScope;
    private final Map<String, Identity<DeclarationNode>> variables;
    private final Set<String> allVariablesDeclaredInScope;

    public SubScope(Scope parentScope, Set<String> allVariablesDeclaredInScope) {
        this(parentScope, allVariablesDeclaredInScope, new HashMap<String, Identity<DeclarationNode>>());
    }
    
    private SubScope(Scope parentScope, Set<String> allVariablesDeclaredInScope, Map<String, Identity<DeclarationNode>> variables) {
        this.parentScope = parentScope;
        this.allVariablesDeclaredInScope = allVariablesDeclaredInScope;
        this.variables = variables;
    }
    
    public void add(String identifier, DeclarationNode node) {
        if (allVariablesDeclaredInScope.contains(identifier)) {
            variables.put(identifier, new Identity<DeclarationNode>(node));            
        } else {
            throw new RuntimeException("Variable has not been pre-declared: " + identifier);
        }
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
    
    public SubScope extend(Set<String> extraVariablesDeclared) {
        HashMap<String, Identity<DeclarationNode>> extendedVariables = new HashMap<String, Identity<DeclarationNode>>(variables);
        Set<String> extendedDeclarations = new HashSet<String>(allVariablesDeclaredInScope);
        extendedDeclarations.addAll(extraVariablesDeclared);
        return new SubScope(parentScope, extendedDeclarations, extendedVariables);
    }
}
