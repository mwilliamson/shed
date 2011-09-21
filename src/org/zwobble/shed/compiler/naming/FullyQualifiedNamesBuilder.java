package org.zwobble.shed.compiler.naming;

import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.TypeDeclarationNode;

import com.google.common.collect.ImmutableMap;

public class FullyQualifiedNamesBuilder {
    private final ImmutableMap.Builder<Identity<TypeDeclarationNode>, FullyQualifiedName> names = ImmutableMap.builder();
    
    public void addFullyQualifiedName(TypeDeclarationNode node, FullyQualifiedName name) {
        names.put(new Identity<TypeDeclarationNode>(node), name);
    }
    
    public FullyQualifiedNames build() {
        return new FullyQualifiedNames(names.build());
    }
}
