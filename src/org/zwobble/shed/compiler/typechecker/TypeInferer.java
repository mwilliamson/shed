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
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeReferenceNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.TypeApplication;

import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;
import static org.zwobble.shed.compiler.typechecker.VariableLookup.lookupVariableReference;

public class TypeInferer {
    private final NodeLocations nodeLocations;
    private final TypeLookup typeLookup;

    public TypeInferer(NodeLocations nodeLocations, TypeLookup typeLookup) {
        this.nodeLocations = nodeLocations;
        this.typeLookup = typeLookup;
    }
    
    public TypeResult<Type> inferType(ExpressionNode expression, StaticContext context) {
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
            return inferType((ShortLambdaExpressionNode)expression, context);
        }
        if (expression instanceof LongLambdaExpressionNode) {
            return inferLongLambdaExpressionType((LongLambdaExpressionNode)expression, context);
        }
        throw new RuntimeException("Cannot infer type of expression: " + expression);
    }

    private TypeResult<Type> inferType(ShortLambdaExpressionNode lambdaExpression, StaticContext context) {
        List<CompilerError> errors = new ArrayList<CompilerError>();

        TypeResult<List<Type>> typeParametersResult = inferArgumentTypes(lambdaExpression.getFormalArguments(), context);
        errors.addAll(typeParametersResult.getErrors());
        
        TypeResult<Type> expressionTypeResult = inferType(lambdaExpression.getBody(), context);
        errors.addAll(expressionTypeResult.getErrors());
        
        Option<TypeReferenceNode> returnTypeReference = lambdaExpression.getReturnType();
        if (returnTypeReference.hasValue()) {
            TypeResult<Type> returnTypeResult = typeLookup.lookupTypeReference(returnTypeReference.get(), context);
            errors.addAll(returnTypeResult.getErrors());
            if (returnTypeResult.isSuccess() && !expressionTypeResult.get().equals(returnTypeResult.get())) {
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
        List<Type> typeParameters = typeParametersResult.get();
        typeParameters.add(expressionTypeResult.get());
        
        return success((Type)new TypeApplication(CoreTypes.functionType(lambdaExpression.getFormalArguments().size()), typeParameters));
    }

    private TypeResult<Type> inferLongLambdaExpressionType(LongLambdaExpressionNode lambdaExpression, StaticContext context) {
        List<CompilerError> errors = new ArrayList<CompilerError>();

        TypeResult<List<Type>> typeParametersResult = inferArgumentTypes(lambdaExpression.getFormalArguments(), context);
        errors.addAll(typeParametersResult.getErrors());
        
        TypeResult<Type> returnTypeResult = typeLookup.lookupTypeReference(lambdaExpression.getReturnType(), context);
        errors.addAll(returnTypeResult.getErrors());
        
        if (errors.isEmpty()) {
            List<Type> typeParameters = typeParametersResult.get();
            typeParameters.add(returnTypeResult.get());
            return success((Type)new TypeApplication(CoreTypes.functionType(lambdaExpression.getFormalArguments().size()), typeParameters));
        } else {
            return failure(errors);
        }
    }
    
    private TypeResult<List<Type>> inferArgumentTypes(List<FormalArgumentNode> formalArguments, StaticContext context) {
        List<Type> typeParameters = new ArrayList<Type>();
        List<CompilerError> errors = new ArrayList<CompilerError>();
        for (FormalArgumentNode argument : formalArguments) {
            TypeResult<Type> argumentType = typeLookup.lookupTypeReference(argument.getType(), context);
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
