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

import com.google.common.base.Function;

import static com.google.common.collect.Lists.transform;
import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.typechecker.TypeChecker.typeCheckStatement;
import static org.zwobble.shed.compiler.typechecker.TypeLookup.lookupTypeReference;
import static org.zwobble.shed.compiler.typechecker.TypeResult.combine;
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
        context.enterNewScope(none(Type.class));
        
        TypeResult<List<Type>> argumentTypesResult = inferArgumentTypesAndAddToContext(lambdaExpression.getFormalArguments(), nodeLocations, context);
        errors.addAll(argumentTypesResult.getErrors());
        
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
    inferLongLambdaExpressionType(final LongLambdaExpressionNode lambdaExpression, final NodeLocations nodeLocations, final StaticContext context) {
        TypeResult<Type> returnTypeResult = lookupTypeReference(lambdaExpression.getReturnType(), nodeLocations, context);
        return returnTypeResult.ifValueThen(new Function<Type, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(Type returnType) {
                context.enterNewScope(some(returnType));
                TypeResult<List<Type>> argumentTypesResult = inferArgumentTypesAndAddToContext(lambdaExpression.getFormalArguments(), nodeLocations, context);
                TypeResult<Type> result = argumentTypesResult
                    .ifValueThen(buildFunctionType(returnType));
                
                for (StatementNode statement : lambdaExpression.getBody()) {
                    TypeResult<Void> statementResult = typeCheckStatement(statement, nodeLocations, context);
                    result = result.withErrorsFrom(statementResult);
                }
                context.exitScope();
                return result;
            }
        });
    }
    
    private static Function<List<Type>, TypeResult<Type>> buildFunctionType(final Type returnType) {
        return new Function<List<Type>, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(List<Type> argumentTypes) {
                List<Type> typeParameters = new ArrayList<Type>(argumentTypes);
                typeParameters.add(returnType);
                return success((Type)new TypeApplication(CoreTypes.functionType(argumentTypes.size()), typeParameters));
            }
        };
    }

    private static TypeResult<List<Type>>
    inferArgumentTypesAndAddToContext(List<FormalArgumentNode> formalArguments, NodeLocations nodeLocations, StaticContext context) {
        return combine(transform(formalArguments, inferArgumentTypeAndAddToContext(nodeLocations, context)));
    }

    private static Function<FormalArgumentNode, TypeResult<Type>>
    inferArgumentTypeAndAddToContext(final NodeLocations nodeLocations, final StaticContext context) {
        return new Function<FormalArgumentNode, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(FormalArgumentNode argument) {
                TypeResult<Type> lookupTypeReference = lookupTypeReference(argument.getType(), nodeLocations, context);
                lookupTypeReference.ifValueThen(addArgumentToContext(argument.getName(), context));
                return lookupTypeReference;
            }
        };
    }

    private static Function<Type, TypeResult<Void>> addArgumentToContext(final String name, final StaticContext context) {
        return new Function<Type, TypeResult<Void>>() {
            @Override
            public TypeResult<Void> apply(Type argumentType) {
                context.add(name, argumentType);
                return success(null);
            }
        };
    }
    
}
