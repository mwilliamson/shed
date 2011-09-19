package org.zwobble.shed.compiler.referenceresolution;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.zwobble.shed.compiler.parsing.nodes.BlockNode;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.IfThenElseStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.LambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;

public class DeclarationFinder {
    public Set<String> findDeclarations(SyntaxNode node) {
        if (isLiteralNode(node) || 
            node instanceof ExpressionStatementNode || 
            node instanceof VariableIdentifierNode || 
            node instanceof ReturnNode ||
            node instanceof LambdaExpressionNode ||
            node instanceof IfThenElseStatementNode
        ) {
            return Collections.emptySet();
        }
        if (node instanceof DeclarationNode) {
            return Collections.singleton(((DeclarationNode) node).getIdentifier());
        }
        if (node instanceof PublicDeclarationNode) {
            return findDeclarations(((PublicDeclarationNode) node).getDeclaration());
        }
        if (node instanceof BlockNode) {
            Set<String> declarations = new HashSet<String>();
            for (StatementNode child : (BlockNode)node) {
                declarations.addAll(findDeclarations(child));
            }
            return declarations;
        }
        if (node instanceof SourceNode) {
            Set<String> declarations = new HashSet<String>();
            SourceNode source = (SourceNode) node;
            for (ImportNode importNode : source.getImports()) {
                declarations.addAll(findDeclarations(importNode));
            }
            return declarations;
        }
        throw new RuntimeException("Don't how to find declarations for: " + node);
    }

    private boolean isLiteralNode(SyntaxNode node) {
        return node instanceof BooleanLiteralNode || node instanceof NumberLiteralNode || node instanceof StringLiteralNode;
    }
}
