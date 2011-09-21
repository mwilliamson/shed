package org.zwobble.shed.compiler.naming;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.TypeDeclarationNode;

@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class FullyQualifiedNames {
    private final Map<Identity<TypeDeclarationNode>, FullyQualifiedName> names;
    
    public FullyQualifiedName fullyQualifiedNameOf(TypeDeclarationNode node) {
        return names.get(new Identity<TypeDeclarationNode>(node));
    }
}
