package org.zwobble.shed.compiler.referenceresolution;

import org.zwobble.shed.compiler.parsing.nodes.BlockNode;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;

public class ReferenceResolver {
    public References resolveReferences(SyntaxNode node) {
        ReferencesBuilder references = new ReferencesBuilder();
        resolveReferences(node, references, new SubScope(new TopScope()));
        return references.build();
    }
    
    private void resolveReferences(SyntaxNode node, ReferencesBuilder references, SubScope scope) {
        if (node instanceof VariableIdentifierNode) {
            VariableIdentifierNode variableIdentifier = (VariableIdentifierNode) node;
            references.addReference(variableIdentifier, scope.lookup(variableIdentifier.getIdentifier()));
        } else if (isLiteralNode(node)) {
            // Do nothing
        } else if (node instanceof BlockNode) {
            for (StatementNode child : (BlockNode)node) {
                resolveReferences(child, references, scope);
            }
        } else if (node instanceof DeclarationNode) {
            DeclarationNode declaration = (DeclarationNode)node;
            scope.add(declaration.getIdentifier(), declaration);
        } else if (node instanceof ExpressionStatementNode) {
            resolveReferences(((ExpressionStatementNode) node).getExpression(), references, scope);
        } else {
            throw new RuntimeException("Don't how to resolve references for: " + node);
        }
    }

    private boolean isLiteralNode(SyntaxNode node) {
        return node instanceof BooleanLiteralNode || node instanceof NumberLiteralNode || node instanceof StringLiteralNode;
    }
}
