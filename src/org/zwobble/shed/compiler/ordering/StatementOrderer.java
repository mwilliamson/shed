package org.zwobble.shed.compiler.ordering;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.referenceresolution.References;
import org.zwobble.shed.compiler.typechecker.TypeResult;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.contains;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static org.zwobble.shed.compiler.parsing.nodes.NodeNavigator.descendents;

public class StatementOrderer {
    public TypeResult<Iterable<StatementNode>> reorder(Iterable<StatementNode> statements, References references) {
        List<StatementNode> orderedStatements = new LinkedList<StatementNode>();
        Iterables.addAll(orderedStatements, filter(statements, not(reorderable())));
        
        insertReorderableStatements(statements, orderedStatements, references);
        
        return TypeResult.<Iterable<StatementNode>>success(orderedStatements);
    }


    private void insertReorderableStatements(Iterable<StatementNode> statements, List<StatementNode> orderedStatements, References references) {
        Iterable<StatementNode> reorderableStatements = filter(statements, reorderable());
        for (StatementNode reorderableStatement : reorderableStatements) {
            insertReorderableStatement(reorderableStatement, orderedStatements, references);
        }
    }
    
    
    private void insertReorderableStatement(StatementNode reorderableStatement, List<StatementNode> orderedStatements, References references) {
        ListIterator<StatementNode> iterator = orderedStatements.listIterator(orderedStatements.size());
        while (iterator.hasPrevious() && contains(referredDeclarations(iterator.previous(), references), reorderableStatement)) {
        }
        iterator.add(reorderableStatement);
    }

    private Iterable<DeclarationNode> referredDeclarations(SyntaxNode node, References references) {
        return transform(variableReferences(node, references), toReferredDeclaration(references));
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

    private Predicate<StatementNode> reorderable() {
        return new Predicate<StatementNode>() {
            @Override
            public boolean apply(StatementNode input) {
                return input instanceof FunctionDeclarationNode;
            }
        };
    }
}
