package org.zwobble.shed.compiler.ordering;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import lombok.Data;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.CompilerErrorDescription;
import org.zwobble.shed.compiler.ordering.errors.CircularDependencyError;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.referenceresolution.VariableNotDeclaredYetError;
import org.zwobble.shed.compiler.typechecker.TypeResult;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static com.google.common.collect.Iterables.get;

import static com.google.common.collect.Iterables.all;
import static com.google.common.collect.Iterables.skip;
import static org.zwobble.shed.compiler.Eager.transform;
import static org.zwobble.shed.compiler.ShedIterables.first;

public class DependencyGraphLineariser {

    public TypeResult<Iterable<StatementNode>> linearise(DependencyGraph graph, NodeLocations nodeLocations) {
        return new Visitor(graph, nodeLocations).visitAll();
    }
    
    @Data
    private static class Dependent {
        private final Identity<StatementNode> statement;
        private final DependencyType dependencyType;
    }
    
    private static class Visitor {
        private final List<StatementNode> ordered = new ArrayList<StatementNode>();
        private final Set<Identity<StatementNode>> declared = Sets.newHashSet();
        private final Queue<Dependent> dependents = Lists.newLinkedList();
        private final DependencyGraph graph;
        private TypeResult<Void> result = TypeResult.success();
        private final NodeLocations nodeLocations;
        
        public Visitor(DependencyGraph graph, NodeLocations nodeLocations) {
            this.graph = graph;
            this.nodeLocations = nodeLocations;
        }
        
        public TypeResult<Iterable<StatementNode>> visitAll() {
            for (StatementNode statement : graph.getStatements()) {
                visit(statement);
            }
            return TypeResult.<Iterable<StatementNode>>success(ordered).withErrorsFrom(result);
        }
        
        private void visit(StatementNode statement) {
            if (isAlreadyDeclared(statement)) {
                return;
            }
            if (isBeingDeclared(statement)) {
                addCircularDependencyError(statement);
                return;
            }
            for (Dependency dependency : graph.dependenciesOf(statement)) {
                dependents.add(new Dependent(identity(statement), dependency.getType()));
                visit(dependency.getStatement());
                dependents.remove();
            }
            declared.add(identity(statement));
            ordered.add(statement);
        }

        private boolean isAlreadyDeclared(StatementNode statement) {
            return declared.contains(identity(statement));
        }

        private boolean isBeingDeclared(StatementNode statement) {
            return Iterables.any(dependents, isStatement(statement));
        }

        private Predicate<Dependent> isStatement(final StatementNode statement) {
            return new Predicate<Dependent>() {
                @Override
                public boolean apply(Dependent input) {
                    return input.getStatement().equals(new Identity<StatementNode>(statement));
                }
            };
        }

        private void addCircularDependencyError(StatementNode statement) {
            CompilerErrorDescription description = describeCircularDependencyError();
            result = result.withErrorsFrom(TypeResult.failure(new CompilerError(
                nodeLocations.locate(statement),
                description
            )));
        }

        private CompilerErrorDescription describeCircularDependencyError() {
            if (all(skip(dependents, 1), isLexicalDependency()) && isStrictLogicalDependency(first(dependents))) {
                DeclarationNode declaration = (DeclarationNode) get(dependents, 1).getStatement().get();
                return new VariableNotDeclaredYetError(declaration.getIdentifier());
            } else {
                return new CircularDependencyError(transform(dependents, toStatement()));
            }
        }

        private Predicate<Dependent> isLexicalDependency() {
            return isDependencyType(DependencyType.LEXICAL);
        }

        private boolean isStrictLogicalDependency(Dependent dependent) {
            return isDependencyType(DependencyType.STRICT_LOGICAL).apply(dependent);
        }

        private Predicate<Dependent> isDependencyType(final DependencyType requiredType) {
            return new Predicate<Dependent>() {
                @Override
                public boolean apply(Dependent input) {
                    return input.getDependencyType() == requiredType;
                }
            };
        }

        private Function<Dependent, StatementNode> toStatement() {
            return new Function<Dependent, StatementNode>() {
                @Override
                public StatementNode apply(Dependent input) {
                    return input.getStatement().get();
                }
            };
        }
    }

    private static Identity<StatementNode> identity(StatementNode statement) {
        return new Identity<StatementNode>(statement);
    }
}
