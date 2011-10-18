package org.zwobble.shed.compiler.typechecker.statements;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionDeclarationNode;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeInferer;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typechecker.VariableLookupResult;
import org.zwobble.shed.compiler.typechecker.VariableLookupResult.Status;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Function;

import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class FunctionDeclarationTypeChecker implements DeclarationTypeChecker<FunctionDeclarationNode> {
    private final TypeInferer typeInferer;
    private final StaticContext context;

    @Inject
    public FunctionDeclarationTypeChecker(TypeInferer typeInferer, StaticContext context) {
        this.typeInferer = typeInferer;
        this.context = context;
    }
    
    @Override
    public TypeResult<?> forwardDeclare(FunctionDeclarationNode functionDeclaration) {
        TypeResult<ValueInfo> typeResult = typeInferer.inferFunctionType(functionDeclaration);
        typeResult.ifValueThen(addToContext(functionDeclaration));
        return typeResult;
    }
    
    @Override
    public TypeResult<StatementTypeCheckResult> typeCheck(FunctionDeclarationNode functionDeclaration, Option<Type> returnType) {
        VariableLookupResult functionLookupResult = context.get(functionDeclaration);
        TypeResult<StatementTypeCheckResult> result = TypeResult.success(StatementTypeCheckResult.noReturn());
        if (functionLookupResult.getStatus() == Status.SUCCESS) {
            TypeResult<ValueInfo> bodyResult = 
                typeInferer.typeCheckBody(functionDeclaration).apply(functionLookupResult.getValueInfo());
            result = result.withErrorsFrom(bodyResult);
        }
        return result;
    }
    
    private Function<ValueInfo, TypeResult<Void>> addToContext(final DeclarationNode declaration) {
        return new Function<ValueInfo, TypeResult<Void>>() {
            @Override
            public TypeResult<Void> apply(ValueInfo input) {
                context.add(declaration, input);
                return success();
            }
        };
    }
}
