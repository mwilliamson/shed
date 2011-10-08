package org.zwobble.shed.compiler.ordering;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;

@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class Dependency {
    public static Dependency lexical(StatementNode statement) {
        return new Dependency(identity(statement), DependencyType.LEXICAL);
    }

    public static Dependency strictLogical(StatementNode statement) {
        return new Dependency(identity(statement), DependencyType.STRICT_LOGICAL);
    }
    
    private static Identity<StatementNode> identity(StatementNode statement) {
        return new Identity<StatementNode>(statement);
    }
    
    private final Identity<StatementNode> statement;
    @Getter
    private final DependencyType type;

    public StatementNode getStatement() {
        return statement.get();
    }
}
