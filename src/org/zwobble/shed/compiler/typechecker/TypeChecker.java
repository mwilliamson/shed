package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.List;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.SourceRange;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.IfThenElseStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.ObjectDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.WhileStatementNode;
import org.zwobble.shed.compiler.typechecker.errors.ConditionNotBooleanError;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import static org.zwobble.shed.compiler.typechecker.ImportStatementTypeChecker.typeCheckImportStatement;
import static org.zwobble.shed.compiler.typechecker.ReturnStatementTypeChecker.typeCheckReturnStatement;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
import static org.zwobble.shed.compiler.typechecker.VariableDeclarationTypeChecker.typeCheckVariableDeclaration;

public class TypeChecker {
    public static TypeResult<Void> typeCheck(SourceNode source, NodeLocations nodeLocations, StaticContext staticContext) {
        List<CompilerError> errors = new ArrayList<CompilerError>();
        
        for (ImportNode importNode : source.getImports()) {
            TypeResult<Void> importTypeCheckResult = typeCheckImportStatement(importNode, nodeLocations, staticContext);
            errors.addAll(importTypeCheckResult.getErrors());
        }

        TypeResult<?> blockResult = typeCheckBlock(source.getStatements(), nodeLocations, staticContext, Option.<Type>none());
        errors.addAll(blockResult.getErrors());
        
        boolean seenPublicStatement = false;
        for (StatementNode statement : source.getStatements()) {
            if (statement instanceof PublicDeclarationNode) {
                if (seenPublicStatement) {
                    errors.add(CompilerError.error(nodeLocations.locate(statement), "A module may have no more than one public value"));
                }
                seenPublicStatement = true;
            }
        }
        
        if (errors.isEmpty()) {
            return success(null);
        } else {
            return failure(errors);
        }
    }
    
    public static TypeResult<StatementTypeCheckResult> typeCheckStatement(
        StatementNode statement, NodeLocations nodeLocations, StaticContext context, Option<Type> returnType
    ) {
        if (statement instanceof VariableDeclarationNode) {
            return typeCheckVariableDeclaration((VariableDeclarationNode)statement, nodeLocations, context);
        }
        if (statement instanceof ReturnNode) {
            return typeCheckReturnStatement((ReturnNode)statement, nodeLocations, context, returnType);
        }
        if (statement instanceof ExpressionStatementNode) {
            TypeResult<Type> result = TypeInferer.inferType(((ExpressionStatementNode) statement).getExpression(), nodeLocations, context);
            return TypeResult.success(StatementTypeCheckResult.noReturn()).withErrorsFrom(result);
        }
        if (statement instanceof ObjectDeclarationNode) {
            return typeCheckObjectDeclaration((ObjectDeclarationNode)statement, nodeLocations, context);
        }
        if (statement instanceof PublicDeclarationNode) {
            return typeCheckStatement(((PublicDeclarationNode) statement).getDeclaration(), nodeLocations, context, returnType);
        }
        if (statement instanceof IfThenElseStatementNode) {
            return typeCheckIfThenElse((IfThenElseStatementNode)statement, nodeLocations, context, returnType);
        }
        if (statement instanceof WhileStatementNode) {
            return typeCheckWhile((WhileStatementNode)statement, nodeLocations, context, returnType);
        }
        if (statement instanceof FunctionDeclarationNode) {
            return typeCheckFunctionDeclaration((FunctionDeclarationNode)statement, nodeLocations, context);
        }
        throw new RuntimeException("Cannot check type of statement: " + statement);
    }

    public static TypeResult<StatementTypeCheckResult> typeCheckObjectDeclaration(
        ObjectDeclarationNode objectDeclaration,
        NodeLocations nodeLocations,
        StaticContext context
    ) {
        TypeResult<StatementTypeCheckResult> result = TypeResult.success(StatementTypeCheckResult.noReturn());

        TypeResult<StatementTypeCheckResult> blockResult = 
            typeCheckBlock(objectDeclaration.getStatements(), nodeLocations, context, Option.<Type>none());
        result = result.withErrorsFrom(blockResult);
        
        if (result.isSuccess()) {
            Builder<String, ValueInfo> typeBuilder = ImmutableMap.builder();

            for (StatementNode statement : objectDeclaration.getStatements()) {
                if (statement instanceof PublicDeclarationNode) {
                    DeclarationNode declaration = ((PublicDeclarationNode) statement).getDeclaration();
                    typeBuilder.put(declaration.getIdentifier(), context.get(declaration).getValueInfo());
                }
            }
            
            InterfaceType type = new InterfaceType(context.fullyQualifiedNameOf(objectDeclaration), typeBuilder.build());

            context.add(objectDeclaration, unassignableValue(type));
        }
        
        return result;
    }

    public static TypeResult<StatementTypeCheckResult> typeCheckFunctionDeclaration(
        FunctionDeclarationNode functionDeclaration,
        NodeLocations nodeLocations,
        StaticContext context
    ) {
        TypeResult<ValueInfo> typeResult = TypeInferer.inferFunctionType(functionDeclaration, nodeLocations, context);
        return typeResult.ifValueThen(addToContext(functionDeclaration, context));
    }

    private static Function<ValueInfo, TypeResult<StatementTypeCheckResult>> addToContext(
        final DeclarationNode declaration, final StaticContext context
    ) {
        return new Function<ValueInfo, TypeResult<StatementTypeCheckResult>>() {
            @Override
            public TypeResult<StatementTypeCheckResult> apply(ValueInfo input) {
                context.add(declaration, input);
                return success(StatementTypeCheckResult.noReturn());
            }
        };
    }

    private static TypeResult<StatementTypeCheckResult> typeCheckIfThenElse(
        IfThenElseStatementNode statement,
        NodeLocations nodeLocations,
        StaticContext context,
        Option<Type> returnType
    ) {
        TypeResult<Void> conditionResult = typeAndCheckCondition(statement.getCondition(), nodeLocations, context);
        
        TypeResult<StatementTypeCheckResult> ifTrueResult = typeCheckBlock(statement.getIfTrue(), nodeLocations, context, returnType);
        TypeResult<StatementTypeCheckResult> ifFalseResult = typeCheckBlock(statement.getIfFalse(), nodeLocations, context, returnType);
        
        boolean returns = 
            ifTrueResult.hasValue() && ifTrueResult.get().hasReturned() && 
            ifFalseResult.hasValue() && ifFalseResult.get().hasReturned();
        
        return TypeResult.success(StatementTypeCheckResult.doesReturn(returns))
            .withErrorsFrom(conditionResult)
            .withErrorsFrom(ifTrueResult)
            .withErrorsFrom(ifFalseResult);
    }

    private static TypeResult<StatementTypeCheckResult> typeCheckWhile(
        WhileStatementNode statement, NodeLocations nodeLocations, StaticContext context, Option<Type> returnType
    ) {
        TypeResult<?> conditionResult = typeAndCheckCondition(statement.getCondition(), nodeLocations, context);
        TypeResult<?> bodyResult = typeCheckBlock(statement.getBody(), nodeLocations, context, returnType);
        return success(StatementTypeCheckResult.noReturn())
            .withErrorsFrom(conditionResult, bodyResult);
    }

    private static TypeResult<Void> typeAndCheckCondition(
        ExpressionNode condition, NodeLocations nodeLocations, StaticContext context
    ) {
        TypeResult<Type> conditionType = TypeInferer.inferType(condition, nodeLocations, context);
        return conditionType.ifValueThen(checkIsBoolean(nodeLocations.locate(condition)));
    }

    private static Function<Type, TypeResult<Void>> checkIsBoolean(final SourceRange conditionLocation) {
        return new Function<Type, TypeResult<Void>>() {
            @Override
            public TypeResult<Void> apply(Type input) {
                if (SubTyping.isSubType(input, CoreTypes.BOOLEAN)) {
                    return TypeResult.success();
                } else {
                    return TypeResult.failure(new CompilerError(
                        conditionLocation,
                        new ConditionNotBooleanError(input)
                    ));
                }
            }
        };
    }

    public static TypeResult<StatementTypeCheckResult> typeCheckBlock(
        Iterable<StatementNode> statements,
        NodeLocations nodeLocations,
        StaticContext staticContext,
        Option<Type> returnType
    ) {
        return new BlockTypeChecker().typeCheckBlock(statements, staticContext, nodeLocations, returnType);
    }
}
