package org.zwobble.shed.compiler.naming;

import java.util.List;

import org.zwobble.shed.compiler.parsing.nodes.NodeNavigator;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
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
            FullyQualifiedName extendedName = currentName.extend(typeDeclaration.getIdentifier());
            builder.addFullyQualifiedName(typeDeclaration, extendedName);
            generateFullyQualifiedNamesForChildren(node, builder, extendedName);
        } else if (node instanceof SourceNode) {
            SourceNode source = (SourceNode) node;
            List<String> packageNames = source.getPackageDeclaration().getPackageNames();
            generateFullyQualifiedNamesForChildren(node, builder, currentName.extend(packageNames));
        } else {
            generateFullyQualifiedNamesForChildren(node, builder, currentName);
        }
    }

    private void generateFullyQualifiedNamesForChildren(SyntaxNode node, FullyQualifiedNamesBuilder builder,
        FullyQualifiedName currentName) {
        for (SyntaxNode child : NodeNavigator.children(node)) {
            generateFullyQualifiedNames(child, builder, currentName);                
        }
    }
}
