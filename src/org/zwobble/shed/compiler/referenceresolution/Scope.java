package org.zwobble.shed.compiler.referenceresolution;

import lombok.Data;

import org.zwobble.shed.compiler.parsing.nodes.Declaration;

public interface Scope {
    Result lookup(String identifier);
    
    public interface Result {
    }
    
    @Data
    public class Success implements Result {
        private final Declaration declaration;
    }
    
    public class NotInScope implements Result {
    }
}
