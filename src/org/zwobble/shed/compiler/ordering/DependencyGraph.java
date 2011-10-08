package org.zwobble.shed.compiler.ordering;

import java.util.List;

import lombok.Getter;

import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import static com.google.common.collect.Iterables.transform;

public class DependencyGraph {
    @Getter
    private final List<? extends StatementNode> statements;
    private final Multimap<Identity<StatementNode>, Identity<StatementNode>> dependencies = HashMultimap.create();

    public DependencyGraph(List<? extends StatementNode> statements) {
        this.statements = statements;
    }
    
    public void addLexicalDependency(StatementNode dependency, StatementNode dependent) {
        dependencies.put(identity(dependent), identity(dependency));
    }
    
    public void addStrictLogicalDependency(DeclarationNode dependency, StatementNode dependent) {
        dependencies.put(identity(dependent), identity(dependency));
    }

    public Iterable<StatementNode> dependenciesOf(StatementNode statement) {
        return transform(dependencies.get(identity(statement)), toStatement());
    }
    
    private Function<Identity<StatementNode>, StatementNode> toStatement() {
        return new Function<Identity<StatementNode>, StatementNode>() {
            @Override
            public StatementNode apply(Identity<StatementNode> input) {
                return input.get();
            }
        };
    }

    private Identity<StatementNode> identity(StatementNode statement) {
        return new Identity<StatementNode>(statement);
    }
}
