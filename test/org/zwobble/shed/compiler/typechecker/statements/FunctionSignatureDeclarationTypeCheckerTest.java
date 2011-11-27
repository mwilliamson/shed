package org.zwobble.shed.compiler.typechecker.statements;

import org.junit.Test;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.FunctionSignatureDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.typechecker.TypeCheckerTestFixture;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.isSuccess;
import static org.zwobble.shed.compiler.typechecker.TypeResultMatchers.isSuccessWithValue;

public class FunctionSignatureDeclarationTypeCheckerTest {
    private final TypeCheckerTestFixture fixture = TypeCheckerTestFixture.build();
    private final FunctionSignatureDeclarationNode funcSignature = Nodes.funcSignature(
        "alert", asList(Nodes.formalArgument("string", fixture.stringTypeReference())), fixture.unitTypeReference());;
    
    @Test public void
    forwardDeclaringAddsFunctionSignatureToContext() {
        TypeResult<?> result = forwardDeclare(funcSignature);
        
        assertThat(result, isSuccess());
        Type functionType = fixture.context().get(funcSignature).getType();
        assertThat(functionType, is((Type)CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.UNIT)));
    }
    
    @Test public void
    functionSignatureDoesNotReturn() {
        TypeResult<StatementTypeCheckResult> result = typeCheck(funcSignature);
        
        assertThat(result, isSuccessWithValue(StatementTypeCheckResult.noReturn()));
    }
    
    private TypeResult<?> forwardDeclare(FunctionSignatureDeclarationNode declaration) {
        return typeChecker().forwardDeclare(declaration);
    }
    
    private TypeResult<StatementTypeCheckResult> typeCheck(FunctionSignatureDeclarationNode declaration) {
        return typeChecker().typeCheck(declaration, Option.<Type>none());
    }

    private FunctionSignatureDeclarationTypeChecker typeChecker() {
        return fixture.get(FunctionSignatureDeclarationTypeChecker.class);
    }
}
