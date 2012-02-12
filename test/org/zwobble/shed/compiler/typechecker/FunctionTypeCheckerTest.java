package org.zwobble.shed.compiler.typechecker;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalTypeParameterNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionWithBodyNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.types.FormalTypeParameter;
import org.zwobble.shed.compiler.types.FormalTypeParameters;
import org.zwobble.shed.compiler.types.ParameterisedFunctionType;
import org.zwobble.shed.compiler.types.Type;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.isSuccess;
import static org.zwobble.shed.compiler.typechecker.ShedTypeValue.shedTypeValue;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
import static org.zwobble.shed.compiler.types.ScalarFormalTypeParameter.invariantFormalTypeParameter;

public class FunctionTypeCheckerTest {
    private final TypeCheckerTestFixture fixture = TypeCheckerTestFixture.build();
    private final VariableIdentifierNode typeParameterReference = Nodes.id("T");
    private final FormalTypeParameterNode typeParameterDeclaration = Nodes.formalTypeParameter("T");
    private final FormalTypeParameter formalTypeParameter = invariantFormalTypeParameter("T");
    
    public FunctionTypeCheckerTest() {
        fixture.addReference(typeParameterReference, typeParameterDeclaration);
        fixture.addType(typeParameterDeclaration, formalTypeParameter);
        fixture.context().add(typeParameterDeclaration, unassignableValue(formalTypeParameter, shedTypeValue(formalTypeParameter)));
    }
    
    @Test public void
    genericFunctionsAreTypeFunctions() {
        FunctionDeclarationNode func = Nodes.func(
            "identity",
            Nodes.formalTypeParameters(typeParameterDeclaration),
            Nodes.formalArguments(Nodes.formalArgument("value", typeParameterReference)),
            typeParameterReference,
            Nodes.block()
        );
        
        TypeResult<ValueInfo> inferedTypeResult = inferType(func);
        
        assertThat(inferedTypeResult, isSuccess());
        ParameterisedFunctionType inferredType = (ParameterisedFunctionType) inferedTypeResult.getOrThrow().getType();
        FormalTypeParameters typeParameters = inferredType.getFormalTypeParameters();
        assertThat(typeParameters, contains(formalTypeParameter));
        assertThat(inferredType.getFunctionTypeParameters(), is(asList((Type)formalTypeParameter, formalTypeParameter)));
    }
    
    @Test public void
    canTypeCheckBodiesOfGenericFunctions() {
        FormalArgumentNode argumentDeclaration = Nodes.formalArgument("value", typeParameterReference);
        VariableIdentifierNode argumentReference = Nodes.id("value");
        fixture.addReference(argumentReference, argumentDeclaration);
        FunctionDeclarationNode func = Nodes.func(
            "identity",
            Nodes.formalTypeParameters(typeParameterDeclaration),
            Nodes.formalArguments(argumentDeclaration),
            typeParameterReference,
            Nodes.block(Nodes.returnStatement(argumentReference))
        );
        
        assertThat(typeCheckBody(func), isSuccess());
    }
    
    private TypeResult<ValueInfo> inferType(FunctionNode function) {
        return fixture.get(FunctionTypeChecker.class).inferFunctionType(function);
    }
    
    private TypeResult<ValueInfo> typeCheckBody(FunctionWithBodyNode function) {
        FunctionTypeChecker functionTypeChecker = fixture.get(FunctionTypeChecker.class);
        return functionTypeChecker.inferFunctionType(function).ifValueThen(functionTypeChecker.typeCheckBody(function));
    }
}
