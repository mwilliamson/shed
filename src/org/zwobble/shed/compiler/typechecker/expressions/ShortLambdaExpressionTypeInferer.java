package org.zwobble.shed.compiler.typechecker.expressions;

import java.util.List;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.typechecker.ArgumentTypeInferer;
import org.zwobble.shed.compiler.typechecker.SubTyping;
import org.zwobble.shed.compiler.typechecker.TypeInferer;
import org.zwobble.shed.compiler.typechecker.TypeLookup;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.TypeResultBuilder;
import org.zwobble.shed.compiler.typechecker.TypeResultWithValue;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typechecker.errors.TypeMismatchError;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.Types;

import static org.zwobble.shed.compiler.errors.CompilerErrors.error;

import static org.zwobble.shed.compiler.typechecker.TypeResultBuilder.typeResultBuilder;

public class ShortLambdaExpressionTypeInferer implements ExpressionTypeInferer<ShortLambdaExpressionNode> {
    private final ArgumentTypeInferer argumentTypeInferer;
    private final TypeLookup typeLookup;
    private final TypeInferer typeInferer;
    private final SubTyping subTyping;

    @Inject
    public ShortLambdaExpressionTypeInferer(
        ArgumentTypeInferer argumentTypeInferer,
        TypeLookup typeLookup,
        TypeInferer typeInferer,
        SubTyping subTyping
    ) {
        this.argumentTypeInferer = argumentTypeInferer;
        this.typeLookup = typeLookup;
        this.typeInferer = typeInferer;
        this.subTyping = subTyping;
    }
    
    @Override
    public TypeResult<ValueInfo> inferValueInfo(ShortLambdaExpressionNode lambdaExpression) {
        TypeResultBuilder<Void> result = typeResultBuilder();
        
        final TypeResult<List<Type>> argumentTypesResult = 
            argumentTypeInferer.inferArgumentTypesAndAddToContext(lambdaExpression.getFormalArguments());
        result.addErrors(argumentTypesResult);
        
        final TypeResult<Type> bodyTypeResult = typeInferer.inferType(lambdaExpression.getBody());
        result.addErrors(bodyTypeResult);
        Option<Type> returnTypeOption = bodyTypeResult.asOption();
        
        Option<? extends ExpressionNode> returnTypeReference = lambdaExpression.getReturnType();
        if (returnTypeReference.hasValue()) {
            TypeResultWithValue<Type> returnTypeResult = typeLookup.lookupTypeReference(returnTypeReference.get());
            result.addErrors(returnTypeResult);
            returnTypeOption = returnTypeResult.asOption();
            if (bodyTypeResult.hasValue() && returnTypeResult.hasValue()) {
                Type bodyType = bodyTypeResult.getOrThrow();
                Type returnType = returnTypeResult.getOrThrow();
                if (!subTyping.isSubType(bodyType, returnType)) {
                    result.addError(error(lambdaExpression.getBody(), new TypeMismatchError(returnType, bodyType)));
                }
            }
        }
        
        Type type;
        if (argumentTypesResult.hasValue() && returnTypeOption.hasValue()) {
            type = CoreTypes.functionTypeOf(argumentTypesResult.getOrThrow(), returnTypeOption.get());
        } else {
            type = Types.newUnknown();
        }
        return result.buildWithValue(ValueInfo.unassignableValue(type));
    }
}
