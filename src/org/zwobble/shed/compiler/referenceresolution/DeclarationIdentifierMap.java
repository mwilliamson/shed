package org.zwobble.shed.compiler.referenceresolution;

import java.util.HashMap;
import java.util.Map;

import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Identity;

import com.google.common.collect.Maps;

public class DeclarationIdentifierMap {
    public static DeclarationIdentifierMap empty() {
        return new DeclarationIdentifierMap(new HashMap<String, Identity<DeclarationNode>>());
    }
    
    private final Map<String, Identity<DeclarationNode>> declarations;

    private DeclarationIdentifierMap(Map<String, Identity<DeclarationNode>> declarations) {
        this.declarations = declarations;
    }

    public boolean isDeclared(String identifier) {
        return declarations.containsKey(identifier);
    }
    
    public DeclarationNode get(String identifier) {
        if (declarations.containsKey(identifier)) {
            return declarations.get(identifier).get();
        } else {
            return null;
        }
    }
    
    public void put(String identifier, DeclarationNode declaration) {
        if (isDeclared(identifier)) {
            throw new RuntimeException("Cannot redefine identifier in same scope"); 
        }
        declarations.put(identifier, new Identity<DeclarationNode>(declaration));
    }
    
    public DeclarationIdentifierMap extend() {
        return new DeclarationIdentifierMap(Maps.newHashMap(declarations));
    }
}
