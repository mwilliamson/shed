package org.zwobble.shed.compiler.typechecker.expressions;

import java.util.List;

import javax.inject.Inject;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.CompilerErrors;
import org.zwobble.shed.compiler.parsing.nodes.CallNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.typechecker.FunctionTyping;
import org.zwobble.shed.compiler.typechecker.SubTyping;
import org.zwobble.shed.compiler.typechecker.TypeInferer;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typechecker.errors.NotCallableError;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Function;

import static org.zwobble.shed.compiler.CompilerErrors.error;
import static org.zwobble.shed.compiler.typechecker.TypeResults.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResults.success;
import static org.zwobble.shed.compiler.typechecker.ValueInfos.toUnassignableValueInfo;

public class CallExpressionTypeInferer implements ExpressionTypeInferer<CallNode> {
    private final TypeInferer typeInferer;
    private final FunctionTyping functionTyping;
    private final SubTyping subTyping;

    @Inject
    public CallExpressionTypeInferer(TypeInferer typeInferer, FunctionTyping functionTyping, SubTyping subTyping) {
        this.typeInferer = typeInferer;
        this.functionTyping = functionTyping;
        this.subTyping = subTyping;
    }
    
    @Override
    public TypeResult<ValueInfo> inferValueInfo(final CallNode expression) {
        TypeResult<Type> calledTypeResult = typeInferer.inferType(expression.getFunction());
        return calledTypeResult.ifValueThen(new Function<Type, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(Type calledType) {
                if (!functionTyping.isFunction(calledType)) {
                    return failure(error(expression, new NotCallableError(calledType)));
                }
                final List<? extends Type> typeParameters = functionTyping.extractFunctionTypeParameters(calledType).get();
                
                int numberOfFormalAguments = typeParameters.size() - 1;
                int numberOfActualArguments = expression.getArguments().size();
                if (numberOfFormalAguments != numberOfActualArguments) {
                    String errorMessage = "Function requires " + numberOfFormalAguments + " argument(s), but is called with " + numberOfActualArguments;
                    CompilerError error = CompilerErrors.error(expression, errorMessage);
                    return failure(error);
                }
                Type returnType = typeParameters.get(numberOfFormalAguments);
                TypeResult<Type> result = success(returnType);
                for (int i = 0; i < numberOfFormalAguments; i++) {
                    final int index = i;
                    final ExpressionNode argument = expression.getArguments().get(i);
                    result = result.withErrorsFrom(typeInferer.inferType(argument).ifValueThen(new Function<Type, TypeResult<Void>>() {
                        @Override
                        public TypeResult<Void> apply(Type actualArgumentType) {
                            if (subTyping.isSubType(actualArgumentType, typeParameters.get(index))) {
                                return success();
                            } else {
                                return failure(error(argument, ("Expected expression of type " + typeParameters.get(index).shortName() +
                                " as argument " + (index + 1) + ", but got expression of type " + actualArgumentType.shortName())));
                            }
                        }
                    }));
                }
                return result;
            }
        }).ifValueThen(toUnassignableValueInfo());
    }

}
