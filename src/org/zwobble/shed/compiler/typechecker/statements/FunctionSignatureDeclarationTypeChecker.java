package org.zwobble.shed.compiler.typechecker.statements;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionSignatureDeclarationNode;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeInferer;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Function;

import static org.zwobble.shed.compiler.typechecker.TypeResults.success;

public class FunctionSignatureDeclarationTypeChecker implements DeclarationTypeChecker<FunctionSignatureDeclarationNode> {
    private final TypeInferer typeInferer;
    private final StaticContext context;

    @Inject
    public FunctionSignatureDeclarationTypeChecker(TypeInferer typeInferer, StaticContext context) {
        this.typeInferer = typeInferer;
        this.context = context;
    }
    
    @Override
    public TypeResult<?> forwardDeclare(FunctionSignatureDeclarationNode declaration) {
        TypeResult<ValueInfo> typeResult = typeInferer.inferFunctionType(declaration);
        typeResult.ifValueThen(addToContext(declaration));
        return typeResult;
    }
    
    @Override
    public TypeResult<StatementTypeCheckResult> typeCheck(FunctionSignatureDeclarationNode statement, Option<Type> returnType) {
        return success(StatementTypeCheckResult.noReturn());
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
