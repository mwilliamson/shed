package org.zwobble.shed.compiler.typechecker;

import java.util.List;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.BlockNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalTypeParameterNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalTypeParametersNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionWithBodyNode;
import org.zwobble.shed.compiler.typechecker.errors.MissingReturnStatementError;
import org.zwobble.shed.compiler.typechecker.statements.StatementTypeCheckResult;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.FormalTypeParameter;
import org.zwobble.shed.compiler.types.FormalTypeParameters;
import org.zwobble.shed.compiler.types.ParameterisedFunctionType;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.TypeApplication;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import static com.google.common.collect.Iterables.concat;
import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.errors.CompilerErrors.error;
import static org.zwobble.shed.compiler.typechecker.ShedTypeValue.shedTypeValue;
import static org.zwobble.shed.compiler.typechecker.TypeResultBuilder.typeResultBuilder;
import static org.zwobble.shed.compiler.typechecker.TypeResults.success;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
import static org.zwobble.shed.compiler.typechecker.ValueInfos.toUnassignableValueInfo;
import static org.zwobble.shed.compiler.types.FormalTypeParameters.formalTypeParameters;
import static org.zwobble.shed.compiler.types.ScalarFormalTypeParameter.invariantFormalTypeParameter;

public class FunctionTypeChecker {
    private final ArgumentTypeInferer argumentTypeInferer;
    private final TypeLookup typeLookup;
    private final BlockTypeChecker blockTypeChecker;
    private final StaticContext context;

    @Inject
    public FunctionTypeChecker(
            ArgumentTypeInferer argumentTypeInferer, TypeLookup typeLookup, BlockTypeChecker blockTypeChecker, StaticContext context) {
        this.argumentTypeInferer = argumentTypeInferer;
        this.typeLookup = typeLookup;
        this.blockTypeChecker = blockTypeChecker;
        this.context = context;
    }
    
    public TypeResult<ValueInfo> inferFunctionType(final FunctionNode function) {
        Option<FormalTypeParameters> formalTypeParameters = addTypeParametersToContext(function);
        TypeResult<List<Type>> argumentTypeResults = argumentTypeInferer.inferArgumentTypesAndAddToContext(function.getFormalArguments());
        TypeResultWithValue<Type> returnTypeResult = typeLookup.lookupTypeReference(function.getReturnType());
        return tryBuildFunctionType(formalTypeParameters, argumentTypeResults, returnTypeResult);
    }

    private Option<FormalTypeParameters> addTypeParametersToContext(FunctionNode function) {
        Option<FormalTypeParametersNode> formalTypeParametersOption = function.getFormalTypeParameters();
        if (formalTypeParametersOption.hasValue()) {
            List<FormalTypeParameter> formalTypeParameters = Lists.newArrayList();
            FormalTypeParametersNode formalTypeParameterNodes = formalTypeParametersOption.get();
            for (FormalTypeParameterNode formalTypeParameterNode : formalTypeParameterNodes) {
                FormalTypeParameter formalTypeParameter = invariantFormalTypeParameter(formalTypeParameterNode.getIdentifier());
                context.add(formalTypeParameterNode, unassignableValue(CoreTypes.CLASS, shedTypeValue(formalTypeParameter)));
                formalTypeParameters.add(formalTypeParameter);
            }
            return some(formalTypeParameters(formalTypeParameters));
        } else {
            return none();
        }
    }

    public Function<ValueInfo, TypeResult<ValueInfo>> typeCheckBody(final FunctionWithBodyNode function) {
        return new Function<ValueInfo, TypeResult<ValueInfo>>() {
            @Override
            public TypeResult<ValueInfo> apply(ValueInfo returnTypeInfo) {
                List<? extends Type> functionTypeParameters = ((TypeApplication)returnTypeInfo.getType()).getTypeParameters();
                Type returnType = functionTypeParameters.get(functionTypeParameters.size() - 1);
                TypeResultBuilder<ValueInfo> result = typeResultBuilder(returnTypeInfo);

                TypeResultWithValue<StatementTypeCheckResult> blockResult = typeCheckBlock(function.getBody(), some(returnType));
                result.addErrors(blockResult);
                
                if (!blockResult.get().hasReturned() && !returnType.equals(CoreTypes.UNIT)) {
                    result.addErrors(TypeResults.<Type>failure(error(
                        function,
                        new MissingReturnStatementError()
                    )));
                }
                return result.build();
            }
        };
    }

    private TypeResult<ValueInfo> tryBuildFunctionType(
            Option<FormalTypeParameters> formalTypeParameters, final TypeResult<List<Type>> argumentTypeResults, TypeResultWithValue<Type> returnTypeResult) {
        return argumentTypeResults.ifValueThen(buildFunctionType(formalTypeParameters, returnTypeResult.get())).ifValueThen(toUnassignableValueInfo());
    }

    private static Function<List<Type>, TypeResult<Type>> buildFunctionType(final Option<FormalTypeParameters> formalTypeParameters, final Type returnType) {
        return new Function<List<Type>, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(List<Type> argumentTypes) {
                if (formalTypeParameters.hasValue()) {
                    List<Type> functionTypeParameters = ImmutableList.copyOf(concat(argumentTypes, asList(returnType)));
                    return success((Type)new ParameterisedFunctionType(functionTypeParameters, formalTypeParameters.get()));
                } else {
                    return success((Type)CoreTypes.functionTypeOf(argumentTypes, returnType));
                }
            }
        };
    }

    private TypeResultWithValue<StatementTypeCheckResult> typeCheckBlock(BlockNode block, Option<Type> returnType) {
        return blockTypeChecker.forwardDeclareAndTypeCheck(block, returnType);
    }
}
