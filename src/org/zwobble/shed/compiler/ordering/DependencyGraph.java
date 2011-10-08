package org.zwobble.shed.compiler.ordering;

import java.util.List;

import lombok.Getter;

import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class DependencyGraph {
    @Getter
    private final List<? extends StatementNode> statements;
    private final Multimap<Identity<StatementNode>, Dependency> dependencies = HashMultimap.create();

    public DependencyGraph(List<? extends StatementNode> statements) {
        this.statements = statements;
    }
    
    public void addLexicalDependency(StatementNode dependency, StatementNode dependent) {
        dependencies.put(identity(dependent), Dependency.lexical(dependency));
    }

    public void addStrictLogicalDependency(DeclarationNode dependency, StatementNode dependent) {
        dependencies.put(identity(dependent), Dependency.strictLogical(dependency));
    }

    public Iterable<Dependency> dependenciesOf(StatementNode statement) {
        return dependencies.get(identity(statement));
    }

    private Identity<StatementNode> identity(StatementNode statement) {
        return new Identity<StatementNode>(statement);
    }
}
