package org.zwobble.shed.compiler.parsing.nodes.structure;

import lombok.Data;

import static java.util.Arrays.asList;

@Data
public class SyntaxNodeStructure {
    public static final SyntaxNodeStructure LEAF = build();

    public static SyntaxNodeStructure build(Iterable<ScopedNodes> children) {
        return new SyntaxNodeStructure(children);
    }
    
    public static SyntaxNodeStructure build(ScopedNodes... children) {
        return build(asList(children));
    }
    
    private final Iterable<ScopedNodes> children;
}
