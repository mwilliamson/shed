package org.zwobble.shed.compiler.dependencies;

import java.util.Queue;
import java.util.Set;

import org.zwobble.shed.compiler.dependencies.errors.UndeclaredDependenciesError;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.HoistableStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.TypeResults;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static org.zwobble.shed.compiler.util.Eager.transform;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.filter;
import static org.zwobble.shed.compiler.CompilerErrors.error;

public class DependencyGraphChecker {
    public TypeResult<Void> check(Iterable<? extends StatementNode> statements, DependencyGraph graph) {
        return new Visitor(filter(statements, isFixedStatement()), graph).visitAll();
    }
    
    private static Predicate<StatementNode> isFixedStatement() {
        return not(isHoistableStatement());
    }
    
    private static Predicate<StatementNode> isHoistableStatement() {
        return new Predicate<StatementNode>() {
            @Override
            public boolean apply(StatementNode input) {
                return isHoistableStatement(input);
            }
        };
    }
    
    private static boolean isHoistableStatement(StatementNode statement) {
        return statement instanceof HoistableStatementNode;
    }

    private static class Visitor {
        private final Set<Identity<StatementNode>> declared = Sets.newHashSet();
        private final Queue<Identity<DeclarationNode>> dependents = Lists.newLinkedList();
        private TypeResult<Void> result = TypeResults.success();
        private final Iterable<? extends StatementNode> fixedStatements;
        private final DependencyGraph graph;
        
        public Visitor(Iterable<? extends StatementNode> fixedStatements, DependencyGraph graph) {
            this.fixedStatements = fixedStatements;
            this.graph = graph;
        }
        
        public TypeResult<Void> visitAll() {
            for (StatementNode statement : fixedStatements) {
                visitFixedStatement(statement);
            }
            return result;
        }
        
        private void visitFixedStatement(StatementNode statement) {
            for (DeclarationNode dependency : graph.dependenciesOf(statement)) {
                visitDependency(dependency, statement);
            }
            declared.add(new Identity<StatementNode>(statement));
        }

        private void visitDependency(DeclarationNode declaration, StatementNode referringStatement) {
            if (isAlreadyDeclared(declaration) || (isHoistableStatement(declaration) && isBeingDeclared(declaration))) {
                return;
            }
            dependents.add(identity(declaration));
            if (isFixedStatement().apply(declaration)) {
                addDependencyError(referringStatement);
            } else {
                for (DeclarationNode dependency : graph.dependenciesOf(declaration)) {
                    visitDependency(dependency, referringStatement);
                }
            }
            dependents.remove();
        }

        private boolean isAlreadyDeclared(DeclarationNode statement) {
            return declared.contains(identity(statement));
        }

        private boolean isBeingDeclared(DeclarationNode statement) {
            return dependents.contains(identity(statement));
        }

        private void addDependencyError(StatementNode statement) {
            result = result.withErrorsFrom(TypeResults.failure(error(
                statement,
                new UndeclaredDependenciesError(transform(dependents, toIdentifier()))
            )));
        }

        private Function<Identity<DeclarationNode>, String> toIdentifier() {
            return new Function<Identity<DeclarationNode>, String>() {
                @Override
                public String apply(Identity<DeclarationNode> input) {
                    return input.get().getIdentifier();
                }
            };
        }
    }

    private static Identity<DeclarationNode> identity(DeclarationNode declaration) {
        return new Identity<DeclarationNode>(declaration);
    }
}
