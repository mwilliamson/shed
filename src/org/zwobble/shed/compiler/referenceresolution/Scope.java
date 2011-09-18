package org.zwobble.shed.compiler.referenceresolution;

import lombok.Data;

import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;

public interface Scope {
    Result lookup(String identifier);
    
    public interface Result {
    }
    
    @Data
    public class Success implements Result {
        private final DeclarationNode node;
    }
    
    public class NotInScope implements Result {
    }
    
    public class NotDeclaredYet implements Result {
    }
}
