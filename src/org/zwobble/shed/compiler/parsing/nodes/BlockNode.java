package org.zwobble.shed.compiler.parsing.nodes;

import java.util.Iterator;
import java.util.List;

import lombok.Data;

@Data
public class BlockNode implements SyntaxNode, Iterable<StatementNode> {
    private final List<StatementNode> statements;
    
    @Override
    public Iterator<StatementNode> iterator() {
        return statements.iterator();
    }
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(statements);
    }
}
