package org.zwobble.shed.compiler.ordering.errors;

import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import org.zwobble.shed.compiler.CompilerErrorDescription;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class CircularDependencyError implements CompilerErrorDescription {
    private final Iterable<? extends StatementNode> statements;
    
    @Override
    public String describe() {
        // TODO: fill in
        return null;
    }
}
