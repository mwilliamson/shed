package org.zwobble.shed.compiler.dependencies;

import java.util.Set;

import org.zwobble.shed.compiler.parsing.nodes.BlockNode;
import org.zwobble.shed.compiler.parsing.nodes.Declaration;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.referenceresolution.References;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.util.ShedIterables;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static org.zwobble.shed.compiler.parsing.nodes.NodeNavigator.descendents;

public class DependencyChecker {
    public TypeResult<Void> check(SyntaxNode node, References references) {
        return new Visitor(references).check(node);
    }
    
    private static class Visitor {
        private final DependencyGraphChecker graphChecker = new DependencyGraphChecker();
        private final References references;
        
        public Visitor(References references) {
            this.references = references;
        }
        
        public TypeResult<Void> check(SyntaxNode node) {
            TypeResult<Void> result = TypeResult.success();
            if (node instanceof SourceNode) {
                result = result.withErrorsFrom(checkSourceNode((SourceNode)node));
            }
            result = result.withErrorsFrom(checkBlocksInNode(node));
            return result;
        }

        private TypeResult<?> checkBlocksInNode(SyntaxNode node) {
            return TypeResult.combine(transform(findAllBlocks(node), checkBlock()));
        }

        private Iterable<BlockNode> findAllBlocks(SyntaxNode node) {
            return filter(descendents(node), BlockNode.class);
        }

        private Function<BlockNode, TypeResult<?>> checkBlock() {
            return new Function<BlockNode, TypeResult<?>>() {
                @Override
                public TypeResult<?> apply(BlockNode input) {
                    return checkStatements(input);
                }
            };
        }

        private TypeResult<Void> checkSourceNode(SourceNode node) {
            return checkStatements(node.getStatements());
        }
        
        private TypeResult<Void> checkStatements(Iterable<StatementNode> statements) {
            DependencyGraph graph = new DependencyGraph();
            for (StatementNode statement : statements) {
                addStatementDependencies(graph, statement, statements);
            }
            return graphChecker.check(statements, graph);
        }

        private void addStatementDependencies(DependencyGraph graph, StatementNode statement, Iterable<StatementNode> statements) {
            Set<Identity<StatementNode>> statementIdentities = Sets.newHashSet(transform(statements, Identity.<StatementNode>toIdentity()));
            for (DeclarationNode dependency : findDependenciesDeclaredInBlock(statement, statementIdentities)) {
                graph.addDependency(dependency, statement);
            }
        }

        private Iterable<DeclarationNode> findDependenciesDeclaredInBlock(StatementNode statement, Set<Identity<StatementNode>> statementsInBlock) {
            Iterable<Declaration> dependencies = transform(findFreeVariables(statement), toReferredDeclaration());
            Iterable<Declaration> dependenciesInBlock = filter(dependencies, isDeclaredInBlock(statementsInBlock));
            return ShedIterables.safeCast(dependenciesInBlock, DeclarationNode.class);
        }

        private Predicate<Declaration> isDeclaredInBlock(final Set<Identity<StatementNode>> statementsInBlock) {
            return new Predicate<Declaration>() {
                @Override
                public boolean apply(Declaration input) {
                    return statementsInBlock.contains(new Identity<Declaration>(input));
                }
            };
        }

        private Iterable<VariableIdentifierNode> findFreeVariables(StatementNode statement) {
            return filter(descendents(statement), VariableIdentifierNode.class);
        }

        private Function<VariableIdentifierNode, Declaration> toReferredDeclaration() {
            return new Function<VariableIdentifierNode, Declaration>() {
                @Override
                public Declaration apply(VariableIdentifierNode input) {
                    return references.findReferent(input);
                }
            };
        }
    }
}
