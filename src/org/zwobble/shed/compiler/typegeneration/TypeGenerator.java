package org.zwobble.shed.compiler.typegeneration;

import org.zwobble.shed.compiler.naming.FullyQualifiedNames;
import org.zwobble.shed.compiler.parsing.nodes.ClassDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.InterfaceDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.NodeNavigator;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeDeclarationNode;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.InterfaceType;

public class TypeGenerator {
    private final FullyQualifiedNames names;

    public TypeGenerator(FullyQualifiedNames names) {
        this.names = names;
    }
    
    public TypeStore generateTypes(SyntaxNode node) {
        TypeStore.Builder builder = TypeStore.builder();
        generateTypes(node, builder);
        return builder.build();
    }
    
    private void generateTypes(SyntaxNode node, TypeStore.Builder builder) {
        if (node instanceof TypeDeclarationNode) {
            TypeDeclarationNode typeDeclarationNode = (TypeDeclarationNode) node;
            if (node instanceof ClassDeclarationNode) {
                builder.add(typeDeclarationNode, new ClassType(names.fullyQualifiedNameOf(typeDeclarationNode)));
            } else if (node instanceof InterfaceDeclarationNode) {
                builder.add(typeDeclarationNode, new InterfaceType(names.fullyQualifiedNameOf(typeDeclarationNode)));
            } else {
                throw new RuntimeException("Could not add type for type declaration: " + typeDeclarationNode);
            }
        }
        for (SyntaxNode child : NodeNavigator.children(node)) {
            generateTypes(child, builder);
        }
    }
}
