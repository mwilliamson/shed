package org.zwobble.shed.compiler.typechecker;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.FormalTypeParameterNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.types.FormalTypeParameter;
import org.zwobble.shed.compiler.types.FormalTypeParameters;
import org.zwobble.shed.compiler.types.ParameterisedFunctionType;
import org.zwobble.shed.compiler.types.ScalarFormalTypeParameter.Variance;
import org.zwobble.shed.compiler.types.Type;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.isSuccess;

public class FunctionTypeCheckerTest {
    private final TypeCheckerTestFixture fixture = TypeCheckerTestFixture.build();
    
    @Test public void
    genericFunctionsAreTypeFunctions() {
        VariableIdentifierNode typeParameterReference = Nodes.id("T");
        FormalTypeParameterNode typeParameterDeclaration = Nodes.formalTypeParameter("T");
        FunctionDeclarationNode func = Nodes.func(
            "identity",
            Nodes.formalTypeParameters(typeParameterDeclaration),
            Nodes.formalArguments(Nodes.formalArgument("value", typeParameterReference)),
            typeParameterReference,
            Nodes.block()
        );
        fixture.addReference(typeParameterReference, typeParameterDeclaration);
        
        TypeResult<ValueInfo> inferedTypeResult = inferType(func);
        
        assertThat(inferedTypeResult, isSuccess());
        ParameterisedFunctionType inferredType = (ParameterisedFunctionType) inferedTypeResult.getOrThrow().getType();
        FormalTypeParameters typeParameters = inferredType.getFormalTypeParameters();
        assertThat(size(typeParameters), is(1));
        FormalTypeParameter formalTypeParameter = get(typeParameters, 0);
        assertThat(formalTypeParameter.shortName(), is("T"));
        assertThat(formalTypeParameter.getVariance(), is(Variance.INVARIANT));
        assertThat(inferredType.getFunctionTypeParameters(), is(asList((Type)formalTypeParameter, formalTypeParameter)));
    }
    
    private TypeResult<ValueInfo> inferType(FunctionNode function) {
        return fixture.get(FunctionTypeChecker.class).inferFunctionType(function);
    }
}
