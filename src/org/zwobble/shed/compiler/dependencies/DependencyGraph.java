package org.zwobble.shed.compiler.dependencies;

import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import static org.zwobble.shed.compiler.Eager.transform;

public class DependencyGraph {
    private final Multimap<Identity<StatementNode>, Identity<DeclarationNode>> dependencies = HashMultimap.create();
    
    public void addDependency(DeclarationNode dependency, StatementNode dependent) {
        dependencies.put(identity(dependent), identity(dependency));
    }

    public Iterable<DeclarationNode> dependenciesOf(StatementNode statement) {
        return transform(dependencies.get(identity(statement)), toDeclaration());
    }

    private Function<Identity<DeclarationNode>, DeclarationNode> toDeclaration() {
        return new Function<Identity<DeclarationNode>, DeclarationNode>() {
            @Override
            public DeclarationNode apply(Identity<DeclarationNode> input) {
                return input.get();
            }
        };
    }

    private <T extends StatementNode> Identity<T> identity(T statement) {
        return new Identity<T>(statement);
    }
}
