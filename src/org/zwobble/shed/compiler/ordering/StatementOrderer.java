package org.zwobble.shed.compiler.ordering;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
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
import org.zwobble.shed.compiler.referenceresolution.VariableNotDeclaredYetError;
import org.zwobble.shed.compiler.typechecker.TypeResult;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static org.zwobble.shed.compiler.parsing.nodes.NodeNavigator.descendents;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class StatementOrderer {
    public TypeResult<Iterable<StatementNode>> reorder(
        Iterable<StatementNode> statements, NodeLocations nodeLocations, References references
    ) {
        List<StatementNode> orderedStatements = new LinkedList<StatementNode>();
        Iterables.addAll(orderedStatements, filter(statements, not(reorderable())));
        
        TypeResult<?> nonReorderableResult = checkDeclarationOrder(orderedStatements, nodeLocations, references);
        TypeResult<?> reorderableResult = insertReorderableStatements(statements, orderedStatements, nodeLocations, references);
        
        return TypeResult.<Iterable<StatementNode>>success(orderedStatements).withErrorsFrom(reorderableResult, nonReorderableResult);
    }


    private TypeResult<?> checkDeclarationOrder(List<StatementNode> orderedStatements, NodeLocations nodeLocations, References references) {
        TypeResult<?> result = success();
        Set<Identity<DeclarationNode>> undeclaredVariables = Sets.newHashSet();
        for (StatementNode statement : Lists.reverse(orderedStatements)) {
            if (statement instanceof DeclarationNode) {
                undeclaredVariables.add(new Identity<DeclarationNode>((DeclarationNode)statement));
            }
            for (VariableIdentifierNode reference : variableReferences(statement, references)) {
                if (undeclaredVariables.contains(new Identity<SyntaxNode>(references.findReferent(reference)))) {
                    result = result.withErrorsFrom(failure(
                        new CompilerError(nodeLocations.locate(statement), new VariableNotDeclaredYetError(reference.getIdentifier()))
                    ));
                }
            }
        }
        return result;
    }


    private TypeResult<Void> insertReorderableStatements(
        Iterable<StatementNode> statements, List<StatementNode> orderedStatements, NodeLocations nodeLocations, References references
    ) {
        Iterable<DeclarationNode> reorderableStatements = filterToReorderableStatements(statements);
        Map<Identity<DeclarationNode>, Integer> minPositions = new HashMap<Identity<DeclarationNode>, Integer>();
        Map<Identity<DeclarationNode>, Integer> maxPositions = new HashMap<Identity<DeclarationNode>, Integer>();
        
        TypeResult<Void> result = success();
        for (DeclarationNode reorderableStatement : reorderableStatements) {

            Option<Integer> firstDependentIndex = findFirstDependentIndex(reorderableStatement, orderedStatements, references);
            Option<Integer> lastDependencyIndex = findLastDependencyIndex(reorderableStatement, orderedStatements, references);
            if (firstDependentIndex.hasValue() && lastDependencyIndex.hasValue() && lastDependencyIndex.get() <= firstDependentIndex.get()) {
                return TypeResult.failure(new CompilerError(
                    nodeLocations.locate(reorderableStatement),
                    new UnpullableDeclarationError(
                        reorderableStatement,
                        (DeclarationNode)orderedStatements.get(lastDependencyIndex.get()),
                        orderedStatements.get(firstDependentIndex.get())
                    )
                ));
            }
            Identity<DeclarationNode> reorderableStatementIdentity = new Identity<DeclarationNode>(reorderableStatement);
            if (lastDependencyIndex.hasValue()) {
                minPositions.put(reorderableStatementIdentity, lastDependencyIndex.get() + 1);                
            } else {
                minPositions.put(reorderableStatementIdentity, 0);
            }
            if (firstDependentIndex.hasValue()) {
                maxPositions.put(reorderableStatementIdentity, firstDependentIndex.get());
            } else {
                maxPositions.put(reorderableStatementIdentity, orderedStatements.size());
            }
        }
        List<Entry<Identity<DeclarationNode>, Integer>> positions = newArrayList(minPositions.entrySet());
        Collections.sort(positions, byPositionDescending());
        for (Entry<Identity<DeclarationNode>, Integer> position : positions) {
            orderedStatements.add(position.getValue(), position.getKey().get());
        }
        return result;
    }

    private Comparator<Entry<Identity<DeclarationNode>, Integer>> byPositionDescending() {
        return new Comparator<Map.Entry<Identity<DeclarationNode>,Integer>>() {
            @Override
            public int compare(Entry<Identity<DeclarationNode>, Integer> first, Entry<Identity<DeclarationNode>, Integer> second) {
                return second.getValue().compareTo(first.getValue());
            }
        };
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
