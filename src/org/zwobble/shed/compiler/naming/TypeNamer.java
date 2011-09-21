package org.zwobble.shed.compiler.naming;

import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeDeclarationNode;

public class TypeNamer {
    public FullyQualifiedNames generateFullyQualifiedNames(SyntaxNode node) {
        FullyQualifiedNamesBuilder builder = new FullyQualifiedNamesBuilder();
        generateFullyQualifiedNames(node, builder, FullyQualifiedName.EMPTY);
        return builder.build();
    }
    
    private void generateFullyQualifiedNames(SyntaxNode node, FullyQualifiedNamesBuilder builder, FullyQualifiedName currentName) {
        if (node instanceof TypeDeclarationNode) {
            TypeDeclarationNode typeDeclaration = (TypeDeclarationNode) node;
            builder.addFullyQualifiedName(typeDeclaration, currentName.extend(typeDeclaration.getIdentifier()));
        }
    }
}
