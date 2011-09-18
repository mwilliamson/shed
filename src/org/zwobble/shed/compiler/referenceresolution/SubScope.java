package org.zwobble.shed.compiler.referenceresolution;

import java.util.HashMap;
import java.util.Map;

import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Identity;

public class SubScope implements Scope {
    private final Scope parentScope;
    private final Map<String, Identity<DeclarationNode>> variables = new HashMap<String, Identity<DeclarationNode>>();

    public SubScope(Scope parentScope) {
        this.parentScope = parentScope;
    }
    
    void add(String identifier, DeclarationNode node) {
        variables.put(identifier, new Identity<DeclarationNode>(node));
    }

    public DeclarationNode lookup(String identifier) {
        return variables.get(identifier).get();
    }
}
