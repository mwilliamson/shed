package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.List;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.LongLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeReferenceNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.TypeApplication;

import static org.zwobble.shed.compiler.Option.none;

import static org.zwobble.shed.compiler.Option.some;

import static org.zwobble.shed.compiler.typechecker.TypeChecker.typeCheckStatement;

import static org.zwobble.shed.compiler.typechecker.TypeLookup.lookupTypeReference;

import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;
import static org.zwobble.shed.compiler.typechecker.VariableLookup.lookupVariableReference;

public class TypeInferer {
    public static TypeResult<Type> inferType(ExpressionNode expression, NodeLocations nodeLocations, StaticContext context) {
        if (expression instanceof BooleanLiteralNode) {
            return success(CoreTypes.BOOLEAN);            
        }
        if (expression instanceof NumberLiteralNode) {
            return success(CoreTypes.NUMBER);
        }
        if (expression instanceof StringLiteralNode) {
            return success(CoreTypes.STRING);
        }
        if (expression instanceof VariableIdentifierNode) {
            return lookupVariableReference(((VariableIdentifierNode)expression).getIdentifier(), nodeLocations.locate(expression), context);
        }
        if (expression instanceof ShortLambdaExpressionNode) {
            return inferType((ShortLambdaExpressionNode)expression, nodeLocations, context);
        }
        if (expression instanceof LongLambdaExpressionNode) {
            return inferLongLambdaExpressionType((LongLambdaExpressionNode)expression, nodeLocations, context);
        }
        throw new RuntimeException("Cannot infer type of expression: " + expression);
    }

    private static TypeResult<Type> inferType(ShortLambdaExpressionNode lambdaExpression, NodeLocations nodeLocations, StaticContext context) {
        List<CompilerError> errors = new ArrayList<CompilerError>();

        TypeResult<List<Type>> argumentTypesResult = inferArgumentTypes(lambdaExpression.getFormalArguments(), nodeLocations, context);
        errors.addAll(argumentTypesResult.getErrors());
        
        context.enterNewScope(none(Type.class));
        if (argumentTypesResult.hasValue()) {
            for (int argumentIndex = 0; argumentIndex < lambdaExpression.getFormalArguments().size(); argumentIndex++) {
                String argumentName = lambdaExpression.getFormalArguments().get(argumentIndex).getName();
                context.add(argumentName, argumentTypesResult.get().get(argumentIndex));
            }
        }
        TypeResult<Type> expressionTypeResult = inferType(lambdaExpression.getBody(), nodeLocations, context);
        errors.addAll(expressionTypeResult.getErrors());
        context.exitScope();
        
        Option<TypeReferenceNode> returnTypeReference = lambdaExpression.getReturnType();
        if (returnTypeReference.hasValue()) {
            TypeResult<Type> returnTypeResult = lookupTypeReference(returnTypeReference.get(), nodeLocations, context);
            errors.addAll(returnTypeResult.getErrors());
            if (returnTypeResult.hasValue() && expressionTypeResult.hasValue() && !expressionTypeResult.get().equals(returnTypeResult.get())) {
                errors.add(new CompilerError(
                    nodeLocations.locate(lambdaExpression.getBody()),
                    "Type mismatch: expected expression of type \"" + returnTypeResult.get().shortName() +
                        "\" but was of type \"" + expressionTypeResult.get().shortName() + "\""
                ));
            }
        }
        
        if (!errors.isEmpty()) {
            return failure(errors);
        }
        List<Type> typeParameters = argumentTypesResult.get();
        typeParameters.add(expressionTypeResult.get());
        
        return success((Type)new TypeApplication(CoreTypes.functionType(lambdaExpression.getFormalArguments().size()), typeParameters));
    }

    private static TypeResult<Type>
    inferLongLambdaExpressionType(LongLambdaExpressionNode lambdaExpression, NodeLocations nodeLocations, StaticContext context) {
        List<CompilerError> errors = new ArrayList<CompilerError>();

        TypeResult<List<Type>> argumentTypesResult = inferArgumentTypes(lambdaExpression.getFormalArguments(), nodeLocations, context);
        errors.addAll(argumentTypesResult.getErrors());
        
        TypeResult<Type> returnTypeResult = lookupTypeReference(lambdaExpression.getReturnType(), nodeLocations, context);
        errors.addAll(returnTypeResult.getErrors());

        if (returnTypeResult.hasValue() && argumentTypesResult.hasValue()) {
            context.enterNewScope(some(returnTypeResult.get()));
            for (int argumentIndex = 0; argumentIndex < lambdaExpression.getFormalArguments().size(); argumentIndex++) {
                String argumentName = lambdaExpression.getFormalArguments().get(argumentIndex).getName();
                context.add(argumentName, argumentTypesResult.get().get(argumentIndex));
            }
            for (StatementNode statement : lambdaExpression.getBody()) {
                TypeResult<Void> statementResult = typeCheckStatement(statement, nodeLocations, context);
                errors.addAll(statementResult.getErrors());
            }
            context.exitScope();
        }
        
        if (errors.isEmpty()) {
            List<Type> typeParameters = argumentTypesResult.get();
            typeParameters.add(returnTypeResult.get());
            return success((Type)new TypeApplication(CoreTypes.functionType(lambdaExpression.getFormalArguments().size()), typeParameters));
        } else {
            return failure(errors);
        }
    }
    
    private static TypeResult<List<Type>>
    inferArgumentTypes(List<FormalArgumentNode> formalArguments, NodeLocations nodeLocations, StaticContext context) {
        List<Type> typeParameters = new ArrayList<Type>();
        List<CompilerError> errors = new ArrayList<CompilerError>();
        for (FormalArgumentNode argument : formalArguments) {
            TypeResult<Type> argumentType = lookupTypeReference(argument.getType(), nodeLocations, context);
            errors.addAll(argumentType.getErrors());
            typeParameters.add(argumentType.get());
        }
        if (errors.isEmpty()) {
            return success(typeParameters);
        } else {
            return failure(errors);
        }
    }
    
}
