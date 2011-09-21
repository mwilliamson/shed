package org.zwobble.shed.compiler.naming;

import org.zwobble.shed.compiler.parsing.nodes.TypeDeclarationNode;

public class TypeNamer {
    public FullyQualifiedNames generateFullyQualifiedNames(TypeDeclarationNode node) {
        FullyQualifiedNamesBuilder builder = new FullyQualifiedNamesBuilder();
        generateFullyQualifiedNames(node, builder, FullyQualifiedName.EMPTY);
        return builder.build();
    }
    
    private void generateFullyQualifiedNames(TypeDeclarationNode node, FullyQualifiedNamesBuilder builder, FullyQualifiedName currentName) {
        if (node instanceof TypeDeclarationNode) {
            builder.addFullyQualifiedName(node, currentName.extend(node.getIdentifier()));
        }
    }
}
