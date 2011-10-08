package org.zwobble.shed.compiler.ordering;

import java.util.Queue;
import java.util.Set;

import lombok.Data;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.CompilerErrorDescription;
import org.zwobble.shed.compiler.ordering.errors.CircularDependencyError;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.referenceresolution.VariableNotDeclaredYetError;
import org.zwobble.shed.compiler.typechecker.TypeResult;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.filter;
import static org.zwobble.shed.compiler.Eager.transform;

public class DependencyGraphLineariser {
    public TypeResult<Void> linearise(Iterable<? extends StatementNode> statements, DependencyGraph graph, NodeLocations nodeLocations) {
        return new Visitor(filter(statements, isFixedStatement()), graph, nodeLocations).visitAll();
    }
    
    private static Predicate<StatementNode> isFixedStatement() {
        return not(isReorderableStatement());
    }
    
    private static Predicate<StatementNode> isReorderableStatement() {
        return new Predicate<StatementNode>() {
            @Override
            public boolean apply(StatementNode input) {
                return input instanceof FunctionDeclarationNode;
            }
        };
    }
    
    private static boolean isReorderableStatement(StatementNode statement) {
        return isReorderableStatement().apply(statement);
    }

    @Data
    private static class Dependent {
        private final Identity<StatementNode> statement;
        private final DependencyType dependencyType;
    }
    
    private static class Visitor {
        private final Set<Identity<StatementNode>> declared = Sets.newHashSet();
        private final Queue<Dependent> dependents = Lists.newLinkedList();
        private TypeResult<Void> result = TypeResult.success();
        private final Iterable<? extends StatementNode> fixedStatements;
        private final DependencyGraph graph;
        private final NodeLocations nodeLocations;
        
        public Visitor(Iterable<? extends StatementNode> fixedStatements, DependencyGraph graph, NodeLocations nodeLocations) {
            this.fixedStatements = fixedStatements;
            this.graph = graph;
            this.nodeLocations = nodeLocations;
        }
        
        public TypeResult<Void> visitAll() {
            for (StatementNode statement : fixedStatements) {
                visit(statement);
            }
            return result;
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
                DeclarationNode declaration = dependency.getDeclaration();
                if (isReorderableStatement(declaration)) {
                    
                } else if (!isAlreadyDeclared(declaration) && !isBeingDeclared(declaration)) {
                    CompilerErrorDescription description = new VariableNotDeclaredYetError(declaration.getIdentifier());
                    result = result.withErrorsFrom(TypeResult.failure(new CompilerError(
                        nodeLocations.locate(statement),
                        description
                    )));
                    return;
                }
                dependents.add(new Dependent(identity(statement), dependency.getType()));
                visit(declaration);
                dependents.remove();
            }
            declared.add(identity(statement));
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
            return new CircularDependencyError(transform(dependents, toStatement()));
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
