package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionDeclarationNode;
import org.zwobble.shed.compiler.typechecker.VariableLookupResult.Status;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Function;

import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class FunctionDeclarationTypeChecker implements StatementTypeChecker<FunctionDeclarationNode>, StatementForwardDeclarer<FunctionDeclarationNode> {
    @Override
    public TypeResult<?> forwardDeclare(FunctionDeclarationNode functionDeclaration, NodeLocations nodeLocations, StaticContext context) {
        TypeResult<ValueInfo> typeResult = TypeInferer.inferFunctionType(functionDeclaration, nodeLocations, context);
        typeResult.ifValueThen(addToContext(functionDeclaration, context));
        return typeResult;
    }
    
    @Override
    public TypeResult<StatementTypeCheckResult> typeCheck(
        FunctionDeclarationNode functionDeclaration, NodeLocations nodeLocations, StaticContext context, Option<Type> returnType
    ) {
        VariableLookupResult functionLookupResult = context.get(functionDeclaration);
        TypeResult<StatementTypeCheckResult> result = TypeResult.success(StatementTypeCheckResult.noReturn());
        if (functionLookupResult.getStatus() == Status.SUCCESS) {
            TypeResult<ValueInfo> bodyResult = 
                TypeInferer.typeCheckBody(functionDeclaration, nodeLocations, context).apply(functionLookupResult.getValueInfo());
            result = result.withErrorsFrom(bodyResult);
        }
        return result;
    }
    
    private static Function<ValueInfo, TypeResult<Void>> addToContext(
        final DeclarationNode declaration, final StaticContext context
    ) {
        return new Function<ValueInfo, TypeResult<Void>>() {
            @Override
            public TypeResult<Void> apply(ValueInfo input) {
                context.add(declaration, input);
                return success();
            }
        };
    }
}
