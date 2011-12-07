package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.BlockNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionWithBodyNode;
import org.zwobble.shed.compiler.typechecker.errors.MissingReturnStatementError;
import org.zwobble.shed.compiler.typechecker.statements.StatementTypeCheckResult;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.TypeApplication;

import com.google.common.base.Function;

import static org.zwobble.shed.compiler.typechecker.ValueInfos.toUnassignableValueInfo;

import static org.zwobble.shed.compiler.CompilerErrors.error;
import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.typechecker.TypeResults.success;

public class FunctionTypeChecker {
    private final ArgumentTypeInferer argumentTypeInferer;
    private final TypeLookup typeLookup;
    private final BlockTypeChecker blockTypeChecker;

    @Inject
    public FunctionTypeChecker(ArgumentTypeInferer argumentTypeInferer, TypeLookup typeLookup, BlockTypeChecker blockTypeChecker) {
        this.argumentTypeInferer = argumentTypeInferer;
        this.typeLookup = typeLookup;
        this.blockTypeChecker = blockTypeChecker;
    }
    
    public TypeResult<ValueInfo> inferFunctionType(final FunctionNode function) {
        final TypeResult<List<Type>> argumentTypeResults = argumentTypeInferer.inferArgumentTypesAndAddToContext(function.getFormalArguments());
        TypeResultWithValue<Type> returnTypeResult = typeLookup.lookupTypeReference(function.getReturnType());
        
        return returnTypeResult.ifValueThen(new Function<Type, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(Type returnType) {
                return argumentTypeResults.ifValueThen(buildFunctionType(returnType));
            }
        }).ifValueThen(toUnassignableValueInfo());
    }

    public Function<ValueInfo, TypeResult<ValueInfo>> typeCheckBody(final FunctionWithBodyNode function) {
        return new Function<ValueInfo, TypeResult<ValueInfo>>() {
            @Override
            public TypeResult<ValueInfo> apply(ValueInfo returnTypeInfo) {
                List<? extends Type> functionTypeParameters = ((TypeApplication)returnTypeInfo.getType()).getTypeParameters();
                Type returnType = functionTypeParameters.get(functionTypeParameters.size() - 1);
                TypeResult<ValueInfo> result = success(returnTypeInfo);

                TypeResultWithValue<StatementTypeCheckResult> blockResult = typeCheckBlock(function.getBody(), some(returnType));
                result = result.withErrorsFrom(blockResult);
                
                if (!blockResult.get().hasReturned()) {
                    result = result.withErrorsFrom(TypeResults.<Type>failure(error(
                        function,
                        new MissingReturnStatementError()
                    )));
                }
                return result;
            }
        };
    }

    private static Function<List<Type>, TypeResult<Type>> buildFunctionType(final Type returnType) {
        return new Function<List<Type>, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(List<Type> argumentTypes) {
                List<Type> typeParameters = new ArrayList<Type>(argumentTypes);
                typeParameters.add(returnType);
                return success((Type)CoreTypes.functionTypeOf(typeParameters));
            }
        };
    }

    private TypeResultWithValue<StatementTypeCheckResult> typeCheckBlock(BlockNode block, Option<Type> returnType) {
        return blockTypeChecker.forwardDeclareAndTypeCheck(block, returnType);
    }
}