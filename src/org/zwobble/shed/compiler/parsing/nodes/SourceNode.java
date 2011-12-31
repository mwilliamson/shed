package org.zwobble.shed.compiler.parsing.nodes;

import java.util.List;

import lombok.Data;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.parsing.nodes.structure.SyntaxNodeStructure;

import com.google.common.base.Function;

import static java.util.Collections.singleton;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;

import static com.google.common.collect.Iterables.filter;
import static org.zwobble.shed.compiler.util.ShedIterables.firstOrNone;

import static com.google.common.collect.Iterables.concat;
import static java.util.Collections.singletonList;
import static org.zwobble.shed.compiler.parsing.nodes.structure.ScopedNodes.sameScope;

@Data
public class SourceNode implements SyntaxNode {
    private final PackageDeclarationNode packageDeclaration;
    private final List<ImportNode> imports;
    private final List<StatementNode> statements;
    
    public Option<DeclarationNode> getPublicDeclaration() {
        Iterable<PublicDeclarationNode> publicDeclarations = filter(statements, PublicDeclarationNode.class);
        return firstOrNone(publicDeclarations).map(toDeclaration());
    }
    
    public FullyQualifiedName name() {
        String name = getPublicDeclaration().get().getIdentifier();
        return fullyQualifiedName(concat(packageDeclaration.getPackageNames(), singleton(name)));
    }
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(sameScope(concat(singletonList(packageDeclaration), imports, statements)));
    }
    
    private Function<PublicDeclarationNode, DeclarationNode> toDeclaration() {
        return new Function<PublicDeclarationNode, DeclarationNode>() {
            @Override
            public DeclarationNode apply(PublicDeclarationNode input) {
                return input.getDeclaration();
            }
        };
    }
}
