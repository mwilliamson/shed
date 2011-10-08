package org.zwobble.shed.compiler.ordering;

import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class DependencyGraph {
    private final Multimap<Identity<StatementNode>, Dependency> dependencies = HashMultimap.create();
    
    public void addStrictLogicalDependency(DeclarationNode dependency, StatementNode dependent) {
        dependencies.put(identity(dependent), Dependency.strict(dependency));
    }

    public Iterable<Dependency> dependenciesOf(StatementNode statement) {
        return dependencies.get(identity(statement));
    }

    private Identity<StatementNode> identity(StatementNode statement) {
        return new Identity<StatementNode>(statement);
    }
}
