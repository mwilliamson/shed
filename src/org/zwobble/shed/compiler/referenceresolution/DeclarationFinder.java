package org.zwobble.shed.compiler.referenceresolution;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.zwobble.shed.compiler.parsing.nodes.BlockNode;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.IfThenElseStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;

public class DeclarationFinder {
    public Set<String> findDeclarations(SyntaxNode node) {
        if (node instanceof ExpressionNode || 
            node instanceof ExpressionStatementNode || 
            node instanceof ReturnNode ||
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
            for (StatementNode statement : source.getStatements()) {
                declarations.addAll(findDeclarations(statement));
            }
            return declarations;
        }
        throw new RuntimeException("Don't how to find declarations for: " + node);
    }
}
