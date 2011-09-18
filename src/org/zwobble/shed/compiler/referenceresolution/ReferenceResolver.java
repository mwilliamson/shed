package org.zwobble.shed.compiler.referenceresolution;

import java.util.ArrayList;
import java.util.List;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.BlockNode;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.LambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.referenceresolution.SubScope.NotInScope;
import org.zwobble.shed.compiler.referenceresolution.SubScope.Result;
import org.zwobble.shed.compiler.referenceresolution.SubScope.Success;

public class ReferenceResolver {
    public ReferenceResolverResult resolveReferences(SyntaxNode node, NodeLocations nodeLocations) {
        ReferencesBuilder references = new ReferencesBuilder();
        List<CompilerError> errors = new ArrayList<CompilerError>();
        resolveReferences(node, nodeLocations, references, new SubScope(new TopScope()), errors);
        return ReferenceResolverResult.build(references.build(), errors);
    }
    
    private void resolveReferences(SyntaxNode node, NodeLocations nodeLocations, ReferencesBuilder references, SubScope scope, List<CompilerError> errors) {
        if (node instanceof VariableIdentifierNode) {
            VariableIdentifierNode variableIdentifier = (VariableIdentifierNode) node;
            Result lookupResult = scope.lookup(variableIdentifier.getIdentifier());
            if (lookupResult instanceof NotInScope) {
                errors.add(new CompilerError(nodeLocations.locate(node), new VariableNotInScopeError(variableIdentifier.getIdentifier())));
            } else {
                references.addReference(variableIdentifier, ((Success)lookupResult).getNode());                
            }
            
        } else if (isLiteralNode(node)) {
            // Do nothing
        } else if (node instanceof BlockNode) {
            for (StatementNode child : (BlockNode)node) {
                resolveReferences(child, nodeLocations, references, scope, errors);
            }
        } else if (node instanceof DeclarationNode) {
            DeclarationNode declaration = (DeclarationNode)node;
            if (scope.lookup(declaration.getIdentifier()) instanceof NotInScope) {
                scope.add(declaration.getIdentifier(), declaration);
            } else {
                errors.add(new CompilerError(nodeLocations.locate(node), new DuplicateIdentifierError(declaration.getIdentifier())));
            }
            
        } else if (node instanceof ExpressionStatementNode) {
            resolveReferences(((ExpressionStatementNode) node).getExpression(), nodeLocations, references, scope, errors);
        } else if (node instanceof ShortLambdaExpressionNode) {
            SubScope lambdaScope = new SubScope(scope);
            List<FormalArgumentNode> formalArguments = ((ShortLambdaExpressionNode) node).getFormalArguments();
            for (FormalArgumentNode formalArgument : formalArguments) {
                lambdaScope.add(formalArgument.getIdentifier(), formalArgument);
            }
            resolveReferences(((ShortLambdaExpressionNode) node).getBody(), nodeLocations, references, lambdaScope, errors);
        } else {
            throw new RuntimeException("Don't how to resolve references for: " + node);
        }
    }

    private boolean isLiteralNode(SyntaxNode node) {
        return node instanceof BooleanLiteralNode || node instanceof NumberLiteralNode || node instanceof StringLiteralNode;
    }
}
