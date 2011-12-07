package org.zwobble.shed.compiler.typechecker.statements;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionDeclarationNode;
import org.zwobble.shed.compiler.typechecker.FunctionTypeChecker;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.TypeResultBuilder;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typechecker.VariableLookupResult;
import org.zwobble.shed.compiler.typechecker.VariableLookupResult.Status;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Function;

import static org.zwobble.shed.compiler.typechecker.TypeResultBuilder.typeResultBuilder;
import static org.zwobble.shed.compiler.typechecker.TypeResults.success;

public class FunctionDeclarationTypeChecker implements DeclarationTypeChecker<FunctionDeclarationNode> {
    private final FunctionTypeChecker functionTypeChecker;
    private final StaticContext context;

    @Inject
    public FunctionDeclarationTypeChecker(FunctionTypeChecker functionTypeChecker, StaticContext context) {
        this.functionTypeChecker = functionTypeChecker;
        this.context = context;
    }
    
    @Override
    public TypeResult<?> forwardDeclare(FunctionDeclarationNode functionDeclaration) {
        TypeResult<ValueInfo> typeResult = functionTypeChecker.inferFunctionType(functionDeclaration);
        typeResult.ifValueThen(addToContext(functionDeclaration));
        return typeResult;
    }
    
    @Override
    public TypeResult<StatementTypeCheckResult> typeCheck(FunctionDeclarationNode functionDeclaration, Option<Type> returnType) {
        VariableLookupResult functionLookupResult = context.get(functionDeclaration);
        TypeResultBuilder<StatementTypeCheckResult> result = typeResultBuilder(StatementTypeCheckResult.noReturn());
        if (functionLookupResult.getStatus() == Status.SUCCESS) {
            TypeResult<ValueInfo> bodyResult = 
                functionTypeChecker.typeCheckBody(functionDeclaration).apply(functionLookupResult.getValueInfo());
            result.addErrors(bodyResult);
        }
        return result.build();
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
