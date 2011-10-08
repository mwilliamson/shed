package org.zwobble.shed.compiler.dependencies;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Identity;

@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class Dependency {
    public static Dependency strict(DeclarationNode statement) {
        return new Dependency(identity(statement), DependencyType.STRICT);
    }
    
    private static Identity<DeclarationNode> identity(DeclarationNode statement) {
        return new Identity<DeclarationNode>(statement);
    }
    
    private final Identity<DeclarationNode> declaration;
    @Getter
    private final DependencyType type;

    public DeclarationNode getDeclaration() {
        return declaration.get();
    }
}
