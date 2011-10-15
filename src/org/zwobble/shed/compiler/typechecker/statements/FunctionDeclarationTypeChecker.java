package org.zwobble.shed.compiler.typechecker.statements;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionDeclarationNode;
import org.zwobble.shed.compiler.typechecker.StatementTypeCheckResult;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeInferer;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typechecker.VariableLookupResult;
import org.zwobble.shed.compiler.typechecker.VariableLookupResult.Status;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Function;

import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class FunctionDeclarationTypeChecker implements HoistableStatementTypeChecker<FunctionDeclarationNode> {
    private final TypeInferer typeInferer;

    @Inject
    public FunctionDeclarationTypeChecker(TypeInferer typeInferer) {
        this.typeInferer = typeInferer;
    }
    
    @Override
    public TypeResult<?> forwardDeclare(FunctionDeclarationNode functionDeclaration, StaticContext context) {
        TypeResult<ValueInfo> typeResult = typeInferer.inferFunctionType(functionDeclaration, context);
        typeResult.ifValueThen(addToContext(functionDeclaration, context));
        return typeResult;
    }
    
    @Override
    public TypeResult<StatementTypeCheckResult> typeCheck(
        FunctionDeclarationNode functionDeclaration, StaticContext context, Option<Type> returnType
    ) {
        VariableLookupResult functionLookupResult = context.get(functionDeclaration);
        TypeResult<StatementTypeCheckResult> result = TypeResult.success(StatementTypeCheckResult.noReturn());
        if (functionLookupResult.getStatus() == Status.SUCCESS) {
            TypeResult<ValueInfo> bodyResult = 
                typeInferer.typeCheckBody(functionDeclaration, context).apply(functionLookupResult.getValueInfo());
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
