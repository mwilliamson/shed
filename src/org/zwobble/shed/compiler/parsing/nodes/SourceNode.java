package org.zwobble.shed.compiler.parsing.nodes;

import java.util.List;

import static com.google.common.collect.Iterables.concat;
import static java.util.Collections.singletonList;

import lombok.Data;

@Data
public class SourceNode implements SyntaxNode {
    private final PackageDeclarationNode packageDeclaration;
    private final List<ImportNode> imports;
    private final List<StatementNode> statements;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(concat(singletonList(packageDeclaration), imports, statements));
    }
}
