package org.zwobble.shed.compiler.ordering;

import java.util.Set;

import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.BlockNode;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.Node;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.referenceresolution.References;
import org.zwobble.shed.compiler.typechecker.TypeResult;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static org.zwobble.shed.compiler.parsing.nodes.NodeNavigator.descendents;

public class DependencyChecker {
    public TypeResult<Void> check(SyntaxNode node, References references, NodeLocations nodeLocations) {
        return new Visitor(references, nodeLocations).check(node);
    }
    
    private static class Visitor {
        private final DependencyGraphChecker graphChecker = new DependencyGraphChecker();
        private final References references;
        private final NodeLocations nodeLocations;
        
        public Visitor(References references, NodeLocations nodeLocations) {
            this.references = references;
            this.nodeLocations = nodeLocations;
        }
        
        public TypeResult<Void> check(SyntaxNode node) {
            TypeResult<Void> result = TypeResult.success();
            if (node instanceof SourceNode) {
                result = result.withErrorsFrom(checkSourceNode((SourceNode)node, references, nodeLocations));
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

        private TypeResult<Void> checkSourceNode(SourceNode node, References references, NodeLocations nodeLocations) {
            return checkStatements(node.getStatements());
        }
        
        private TypeResult<Void> checkStatements(Iterable<StatementNode> statements) {
            DependencyGraph graph = new DependencyGraph();
            for (StatementNode statement : statements) {
                addStatementDependencies(graph, statement, statements);
            }
            return graphChecker.check(statements, graph, nodeLocations);
        }

        private void addStatementDependencies(DependencyGraph graph, StatementNode statement, Iterable<StatementNode> statements) {
            Set<Identity<StatementNode>> statementIdentities = Sets.newHashSet(transform(statements, Identity.<StatementNode>toIdentity()));
            for (DeclarationNode dependency : findDependenciesDeclaredInBlock(statement, statementIdentities)) {
                graph.addDependency(dependency, statement);
            }
        }

        private Iterable<DeclarationNode> findDependenciesDeclaredInBlock(StatementNode statement, Set<Identity<StatementNode>> statementsInBlock) {
            return filter(transform(findFreeVariables(statement), toReferredDeclaration()), isDeclaredInBlock(statementsInBlock));
        }

        private Predicate<DeclarationNode> isDeclaredInBlock(final Set<Identity<StatementNode>> statementsInBlock) {
            return new Predicate<DeclarationNode>() {
                @Override
                public boolean apply(DeclarationNode input) {
                    return statementsInBlock.contains(new Identity<Node>(input));
                }
            };
        }

        private Iterable<VariableIdentifierNode> findFreeVariables(StatementNode statement) {
            return filter(descendents(statement), VariableIdentifierNode.class);
        }

        private Function<VariableIdentifierNode, DeclarationNode> toReferredDeclaration() {
            return new Function<VariableIdentifierNode, DeclarationNode>() {
                @Override
                public DeclarationNode apply(VariableIdentifierNode input) {
                    return references.findReferent(input);
                }
            };
        }
    }
}
