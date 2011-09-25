package org.zwobble.shed.compiler.ordering;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.ordering.errors.UnpullableDeclarationError;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.referenceresolution.References;
import org.zwobble.shed.compiler.typechecker.TypeResult;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static org.zwobble.shed.compiler.parsing.nodes.NodeNavigator.descendents;

public class StatementOrderer {
    public TypeResult<Iterable<StatementNode>> reorder(
        Iterable<StatementNode> statements, NodeLocations nodeLocations, References references
    ) {
        List<StatementNode> orderedStatements = new LinkedList<StatementNode>();
        Iterables.addAll(orderedStatements, filter(statements, not(reorderable())));
        
        TypeResult<Void> result = insertReorderableStatements(statements, orderedStatements, nodeLocations, references);
        
        return TypeResult.<Iterable<StatementNode>>success(orderedStatements).withErrorsFrom(result);
    }


    private TypeResult<Void> insertReorderableStatements(
        Iterable<StatementNode> statements, List<StatementNode> orderedStatements, NodeLocations nodeLocations, References references
    ) {
        TypeResult<Void> result = success();
        Iterable<DeclarationNode> reorderableStatements = filterToReorderableStatements(statements);
        for (DeclarationNode reorderableStatement : reorderableStatements) {
            result = result.withErrorsFrom(insertReorderableStatement(reorderableStatement, orderedStatements, nodeLocations, references));
        }
        return result;
    }

    private TypeResult<Void> insertReorderableStatement(
        DeclarationNode reorderableStatement, List<StatementNode> orderedStatements, NodeLocations nodeLocations, References references
    ) {
        Option<Integer> firstDependentIndex = findFirstDependentIndex(reorderableStatement, orderedStatements, references);
        Option<Integer> lastDependencyIndex = findLastDependencyIndex(reorderableStatement, orderedStatements, references);
        if (firstDependentIndex.hasValue()) {
            if (lastDependencyIndex.hasValue() && lastDependencyIndex.get() <= firstDependentIndex.get()) {
                return TypeResult.failure(new CompilerError(
                    nodeLocations.locate(reorderableStatement),
                    new UnpullableDeclarationError(
                        reorderableStatement,
                        orderedStatements.get(lastDependencyIndex.get()),
                        orderedStatements.get(firstDependentIndex.get())
                    )
                ));
            } else {
                orderedStatements.add(firstDependentIndex.get(), reorderableStatement);
                return success();
            }
        } else {
            orderedStatements.add(reorderableStatement);
            return success();
        }
    }


    private Option<Integer> findFirstDependentIndex(
        StatementNode reorderableStatement, List<StatementNode> orderedStatements, References references
    ) {
        ListIterator<StatementNode> iterator = orderedStatements.listIterator();
        while (iterator.hasNext()) {
            Set<Identity<DeclarationNode>> referredDeclarations = referredDeclarations(iterator.next(), references);
            if (referredDeclarations.contains(new Identity<StatementNode>(reorderableStatement))) {
                return Option.some(iterator.previousIndex());
            }
        }
        return Option.none();
    }
    
    private Option<Integer> findLastDependencyIndex(
        StatementNode reorderableStatement, List<StatementNode> orderedStatements, References references
    ) {
        Set<Identity<DeclarationNode>> referredDeclarations = referredDeclarations(reorderableStatement, references);
        ListIterator<StatementNode> iterator = orderedStatements.listIterator(orderedStatements.size());
        while (iterator.hasPrevious()) {
            if (referredDeclarations.contains(new Identity<StatementNode>(iterator.previous()))) {
                return Option.some(iterator.nextIndex());
            }
        }
        return Option.none();
    }

    private Set<Identity<DeclarationNode>> referredDeclarations(SyntaxNode node, References references) {
        Iterable<DeclarationNode> declarations = transform(variableReferences(node, references), toReferredDeclaration(references));
        return Sets.newHashSet(transform(declarations, Identity.<DeclarationNode>toIdentity()));
    }

    private Function<VariableIdentifierNode, DeclarationNode> toReferredDeclaration(final References references) {
        return new Function<VariableIdentifierNode, DeclarationNode>() {
            @Override
            public DeclarationNode apply(VariableIdentifierNode input) {
                return references.findReferent(input);
            }
        };
    }


    private Iterable<VariableIdentifierNode> variableReferences(SyntaxNode node, References references) {
        return filter(descendents(node), VariableIdentifierNode.class);
    }
    
    private Iterable<DeclarationNode> filterToReorderableStatements(Iterable<StatementNode> statements) {
        return filter(filter(statements, DeclarationNode.class), reorderable());
    }

    private Predicate<StatementNode> reorderable() {
        return new Predicate<StatementNode>() {
            @Override
            public boolean apply(StatementNode input) {
                return input instanceof FunctionDeclarationNode;
            }
        };
    }
}
