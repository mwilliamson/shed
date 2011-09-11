package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.IfThenElseStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.ObjectDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementTypeCheckResult;
import org.zwobble.shed.compiler.parsing.nodes.VariableDeclarationNode;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.typechecker.ImportStatementTypeChecker.typeCheckImportStatement;
import static org.zwobble.shed.compiler.typechecker.ReturnStatementTypeChecker.typeCheckReturnStatement;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;
import static org.zwobble.shed.compiler.typechecker.VariableDeclarationTypeChecker.typeCheckVariableDeclaration;

public class TypeChecker {
    public static TypeResult<Void> typeCheck(SourceNode source, NodeLocations nodeLocations, StaticContext staticContext) {
        staticContext.enterNewScope(none(Type.class));
        List<CompilerError> errors = new ArrayList<CompilerError>();
        
        for (ImportNode importNode : source.getImports()) {
            TypeResult<Void> importTypeCheckResult = typeCheckImportStatement(importNode, nodeLocations, staticContext);
            errors.addAll(importTypeCheckResult.getErrors());
        }

        TypeResult<StatementTypeCheckResult> blockResult = typeCheckBlock(source.getStatements(), nodeLocations, staticContext);
        errors.addAll(blockResult.getErrors());
        
        boolean seenPublicStatement = false;
        for (StatementNode statement : source.getStatements()) {
            if (statement instanceof PublicDeclarationNode) {
                if (seenPublicStatement) {
                    errors.add(new CompilerError(nodeLocations.locate(statement), "A module may have no more than one public value"));
                }
                seenPublicStatement = true;
            }
        }
        
        staticContext.exitScope();
        if (errors.isEmpty()) {
            return success(null);
        } else {
            return failure(errors);
        }
    }
    
    public static TypeResult<Void> typeCheckStatement(StatementNode statement, NodeLocations nodeLocations, StaticContext context) {
        if (statement instanceof VariableDeclarationNode) {
            return typeCheckVariableDeclaration((VariableDeclarationNode)statement, nodeLocations, context);
        }
        if (statement instanceof ReturnNode) {
            return typeCheckReturnStatement((ReturnNode)statement, nodeLocations, context);
        }
        if (statement instanceof ExpressionStatementNode) {
            TypeResult<Type> result = TypeInferer.inferType(((ExpressionStatementNode) statement).getExpression(), nodeLocations, context);
            return TypeResult.<Void>success(null).withErrorsFrom(result);
        }
        if (statement instanceof ObjectDeclarationNode) {
            return typeCheckObjectDeclaration((ObjectDeclarationNode)statement, nodeLocations, context);
        }
        if (statement instanceof PublicDeclarationNode) {
            return typeCheckStatement(((PublicDeclarationNode) statement).getDeclaration(), nodeLocations, context);
        }
        if (statement instanceof IfThenElseStatementNode) {
            return typeCheckIfThenElse((IfThenElseStatementNode)statement, nodeLocations, context);
        }
        throw new RuntimeException("Cannot check type of statement: " + statement);
    }

    public static TypeResult<Void> typeCheckObjectDeclaration(
        ObjectDeclarationNode objectDeclaration,
        NodeLocations nodeLocations,
        StaticContext context)
    {
        TypeResult<Void> result = TypeResult.success(null);
        context.enterNewScope(none(Type.class));

        TypeResult<StatementTypeCheckResult> blockResult = new BlockTypeChecker().typeCheckBlock(objectDeclaration.getStatements(), context, nodeLocations);
        result = result.withErrorsFrom(blockResult);
        
        if (result.isSuccess()) {
            Builder<String, Type> typeBuilder = ImmutableMap.builder();

            for (StatementNode statement : objectDeclaration.getStatements()) {
                if (statement instanceof PublicDeclarationNode) {
                    String identifier = ((PublicDeclarationNode) statement).getDeclaration().getIdentifier();
                    typeBuilder.put(identifier, context.get(identifier).getType());
                }
            }
            
            InterfaceType type = new InterfaceType(null, "", typeBuilder.build());

            context.exitScope();
            
            TypeResult<Void> addResult = StaticContexts.tryAdd(
                context,
                objectDeclaration.getIdentifier(),
                type,
                nodeLocations.locate(objectDeclaration)
            );            
            result = result.withErrorsFrom(addResult);
        } else {
            context.exitScope();
        }
        
        return result;
    }

    private static TypeResult<Void> typeCheckIfThenElse(
        IfThenElseStatementNode statement,
        NodeLocations nodeLocations,
        StaticContext context
    ) {
        TypeResult<Type> conditionResult = TypeInferer.inferType(statement.getCondition(), nodeLocations, context);
        TypeResult<StatementTypeCheckResult> ifTrueResult = typeCheckBlock(statement.getIfTrue(), nodeLocations, context);
        TypeResult<StatementTypeCheckResult> ifFalseResult = typeCheckBlock(statement.getIfFalse(), nodeLocations, context);
        return TypeResult.<Void>success(null).withErrorsFrom(TypeResult.combine(Arrays.asList(conditionResult, ifTrueResult, ifFalseResult)));
    }

    private static TypeResult<StatementTypeCheckResult> typeCheckBlock(
        List<StatementNode> statements,
        NodeLocations nodeLocations,
        StaticContext staticContext
    ) {
        return new BlockTypeChecker().typeCheckBlock(statements, staticContext, nodeLocations);
    }
}
