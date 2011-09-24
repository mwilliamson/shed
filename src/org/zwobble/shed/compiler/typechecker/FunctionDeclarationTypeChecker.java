package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.Function0;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionDeclarationNode;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Function;

import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class FunctionDeclarationTypeChecker implements StatementTypeChecker<FunctionDeclarationNode> {
    @Override
    public TypeResult<StatementTypeCheckResult> typeCheck(
        FunctionDeclarationNode functionDeclaration, NodeLocations nodeLocations, StaticContext context, Option<Type> returnType
    ) {

        TypeResult<ValueInfo> typeResult = TypeInferer.inferFunctionType(functionDeclaration, nodeLocations, context);
        typeResult.ifValueThen(addToContext(functionDeclaration, context));
        TypeResult<ValueInfo> bodyResult = typeResult.ifValueThen(TypeInferer.typeCheckBody(functionDeclaration, nodeLocations, context));
        return typeResult.withErrorsFrom(bodyResult).then(new Function0<TypeResult<StatementTypeCheckResult>>() {
            @Override
            public TypeResult<StatementTypeCheckResult> apply() {
                return TypeResult.success(StatementTypeCheckResult.noReturn());
            }
        });
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
