package org.zwobble.shed.compiler.referenceresolution;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.BlockNode;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.CallNode;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.IfThenElseStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.LambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.LongLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.MemberAccessNode;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.parsing.nodes.UnitLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.referenceresolution.Scope.NotDeclaredYet;
import org.zwobble.shed.compiler.referenceresolution.Scope.NotInScope;
import org.zwobble.shed.compiler.referenceresolution.Scope.Result;
import org.zwobble.shed.compiler.referenceresolution.Scope.Success;

import com.google.common.collect.Sets;

public class ReferenceResolver {
    private final DeclarationFinder declarationFinder = new DeclarationFinder();
    
    public ReferenceResolverResult resolveReferences(SyntaxNode node, NodeLocations nodeLocations) {
        ReferencesBuilder references = new ReferencesBuilder();
        List<CompilerError> errors = new ArrayList<CompilerError>();
        resolveReferences(node, nodeLocations, references, new SubScope(new TopScope(), findDeclarations(node)), errors);
        return ReferenceResolverResult.build(references.build(), errors);
    }

    private void resolveReferences(SyntaxNode node, NodeLocations nodeLocations, ReferencesBuilder references, SubScope scope, List<CompilerError> errors) {
        addReferences(node, nodeLocations, references, scope, errors);
        addDeclarations(node, nodeLocations, scope, errors);
    }

    private void addReferences(SyntaxNode node, NodeLocations nodeLocations,
        ReferencesBuilder references, SubScope scope, List<CompilerError> errors) {
        if (node instanceof VariableIdentifierNode) {
            VariableIdentifierNode variableIdentifier = (VariableIdentifierNode) node;
            Result lookupResult = scope.lookup(variableIdentifier.getIdentifier());
            if (lookupResult instanceof NotInScope) {
                errors.add(new CompilerError(nodeLocations.locate(node), new VariableNotInScopeError(variableIdentifier.getIdentifier())));
            } else if (lookupResult instanceof NotDeclaredYet) {
                errors.add(new CompilerError(nodeLocations.locate(node), new VariableNotDeclaredYetError(variableIdentifier.getIdentifier())));
            } else {
                references.addReference(variableIdentifier, ((Success)lookupResult).getNode());                
            }
            
        } else if (isLiteralNode(node)) {
            // Do nothing
        } else if (node instanceof BlockNode) {
            for (StatementNode child : (BlockNode)node) {
                resolveReferences(child, nodeLocations, references, scope, errors);
            }
        } else if (node instanceof ExpressionStatementNode) {
            resolveReferences(((ExpressionStatementNode) node).getExpression(), nodeLocations, references, scope, errors);
        } else if (node instanceof LambdaExpressionNode) {
            LambdaExpressionNode lambda = (LambdaExpressionNode) node;
            SubScope lambdaScope = new SubScope(scope, Sets.union(argumentDeclarations(lambda), findDeclarations(lambda.getBody())));
            List<FormalArgumentNode> formalArguments = lambda.getFormalArguments();
            for (FormalArgumentNode formalArgument : formalArguments) {
                lambdaScope.add(formalArgument.getIdentifier(), formalArgument);
                resolveReferences(formalArgument.getType(), nodeLocations, references, lambdaScope, errors);
            }
            if (node instanceof ShortLambdaExpressionNode) {
                Option<? extends ExpressionNode> returnType = ((ShortLambdaExpressionNode) node).getReturnType();
                if (returnType.hasValue()) {
                    resolveReferences(returnType.get(), nodeLocations, references, scope, errors);                    
                }
            }
            if (node instanceof LongLambdaExpressionNode) {
                resolveReferences(((LongLambdaExpressionNode) node).getReturnType(), nodeLocations, references, scope, errors);
            }
            resolveReferences(lambda.getBody(), nodeLocations, references, lambdaScope, errors);
        } else if (node instanceof ReturnNode) {
            resolveReferences(((ReturnNode) node).getExpression(), nodeLocations, references, scope, errors);
        } else if (node instanceof IfThenElseStatementNode) {
            IfThenElseStatementNode ifElse = (IfThenElseStatementNode) node;
            resolveReferences(ifElse.getCondition(), nodeLocations, references, scope, errors);
            resolveReferences(ifElse.getIfTrue(), nodeLocations, references, scope.extend(findDeclarations(ifElse.getIfTrue())), errors);
            resolveReferences(ifElse.getIfFalse(), nodeLocations, references, scope.extend(findDeclarations(ifElse.getIfFalse())), errors);
        } else if (node instanceof VariableDeclarationNode) {
            resolveReferences(((VariableDeclarationNode) node).getValue(), nodeLocations, references, scope, errors);
        } else if (node instanceof PublicDeclarationNode) {
            resolveReferences(((PublicDeclarationNode) node).getDeclaration(), nodeLocations, references, scope, errors);
        } else if (node instanceof CallNode) {
            CallNode call = (CallNode) node;
            resolveReferences(call.getFunction(), nodeLocations, references, scope, errors);
            for (ExpressionNode argument : call.getArguments()) {
                resolveReferences(argument, nodeLocations, references, scope, errors);
            }
        } else if (node instanceof MemberAccessNode) {
            resolveReferences(((MemberAccessNode) node).getExpression(), nodeLocations, references, scope, errors);
        } else if (node instanceof SourceNode) {
            SourceNode source = (SourceNode) node;
            for (ImportNode importNode : source.getImports()) {
                scope.add(importNode.getIdentifier(), importNode);
            }
            for (StatementNode child : source.getStatements()) {
                resolveReferences(child, nodeLocations, references, scope, errors);
            }
        } else {
            throw new RuntimeException("Don't how to resolve references for: " + node);
        }
    }

    private void addDeclarations(SyntaxNode node, NodeLocations nodeLocations, SubScope scope, List<CompilerError> errors) {
        if (node instanceof DeclarationNode) {
            DeclarationNode declaration = (DeclarationNode)node;
            if (scope.isDeclaredInCurrentScope(declaration.getIdentifier())) {
                errors.add(new CompilerError(nodeLocations.locate(node), new DuplicateIdentifierError(declaration.getIdentifier())));
            } else {
                scope.add(declaration.getIdentifier(), declaration);
            }
        }
    }

    private Set<String> argumentDeclarations(LambdaExpressionNode lambda) {
        Set<String> declarations = new HashSet<String>();
        for (FormalArgumentNode formalArgument : lambda.getFormalArguments()) {
            declarations.add(formalArgument.getIdentifier());
        }
        return declarations;
    }

    private boolean isLiteralNode(SyntaxNode node) {
        return 
            node instanceof BooleanLiteralNode || 
            node instanceof NumberLiteralNode || 
            node instanceof StringLiteralNode ||
            node instanceof UnitLiteralNode;
    }

    private Set<String> findDeclarations(SyntaxNode node) {
        return declarationFinder.findDeclarations(node);
    }
}
