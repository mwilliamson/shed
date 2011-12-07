package org.zwobble.shed.compiler.typechecker.expressions;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.CallNode;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeCheckerTestFixture;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typechecker.errors.NotCallableError;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.errorStrings;
import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration.globalDeclaration;
import static org.zwobble.shed.compiler.typechecker.TypeResultMatchers.isSuccessWithValue;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
import static org.zwobble.shed.compiler.types.Types.typeParameters;

public class CallExpressionTypeInfererTest {
    private final TypeCheckerTestFixture fixture = TypeCheckerTestFixture.build();

    @Test public void
    functionCallsHaveTypeOfReturnTypeOfFunctionWithNoArguments() {
        VariableIdentifierNode reference = Nodes.id("magic");
        GlobalDeclaration declaration = globalDeclaration("magic");
        fixture.addReference(reference, declaration);
        
        StaticContext context = fixture.context();
        context.add(declaration, unassignableValue(CoreTypes.functionTypeOf(CoreTypes.DOUBLE)));
        
        CallNode call = Nodes.call(reference);
        TypeResult<ValueInfo> result = inferType(call);
        assertThat(result, isType(CoreTypes.DOUBLE));
    }
    
    @Test public void
    functionCallsHaveTypeOfReturnTypeOfFunctionWithCorrectArguments() {
        VariableIdentifierNode reference = Nodes.id("isLength");
        GlobalDeclaration declaration = globalDeclaration("isLength");
        fixture.addReference(reference, declaration);
        
        StaticContext context = fixture.context();
        // isLength: (String, Double) -> Boolean 
        context.add(declaration, unassignableValue(CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.DOUBLE, CoreTypes.BOOLEAN)));
        CallNode call = Nodes.call(reference, Nodes.string("Blah"), Nodes.number("4"));
        TypeResult<ValueInfo> result = inferType(call);
        assertThat(result, isType(CoreTypes.BOOLEAN));
    }
    
    @Test public void
    errorIfActualArgumentsAreNotAssignableToFormalArguments() {
        VariableIdentifierNode reference = Nodes.id("isLength");
        GlobalDeclaration declaration = globalDeclaration("isLength");
        fixture.addReference(reference, declaration);
        
        StaticContext context = fixture.context();
        // isLength: (String, Double) -> Boolean 
        context.add(declaration, unassignableValue(CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.DOUBLE, CoreTypes.BOOLEAN)));
        CallNode call = Nodes.call(reference, Nodes.number("4"), Nodes.string("Blah"));
        TypeResult<ValueInfo> result = inferType(call);
        assertThat(
            errorStrings(result),
            is(asList(
                "Expected expression of type String as argument 1, but got expression of type Double",
                "Expected expression of type Double as argument 2, but got expression of type String"
            ))
        );
    }
    
    @Test public void
    cannotCallTypesThatArentFunctions() {
        VariableIdentifierNode reference = Nodes.id("isLength");
        GlobalDeclaration declaration = globalDeclaration("isLength");
        fixture.addReference(reference, declaration);
        
        StaticContext context = fixture.context();
        context.add(declaration, unassignableValue(CoreTypes.BOOLEAN));
        CallNode call = Nodes.call(reference);
        TypeResult<ValueInfo> result = inferType(call);
        assertThat(result, isFailureWithErrors(new NotCallableError(CoreTypes.BOOLEAN)));
    }
    
    @Test public void
    errorIfCallingFunctionWithWrongNumberOfArguments() {
        VariableIdentifierNode reference = Nodes.id("isLength");
        GlobalDeclaration declaration = globalDeclaration("isLength");
        fixture.addReference(reference, declaration);
        
        StaticContext context = fixture.context();
        // isLength: (String, Double) -> Boolean 
        context.add(declaration, unassignableValue(CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.DOUBLE, CoreTypes.BOOLEAN)));
        
        CallNode call = Nodes.call(reference, Nodes.number("4"));
        TypeResult<ValueInfo> result = inferType(call);
        assertThat(
            errorStrings(result),
            is(asList("Function requires 2 argument(s), but is called with 1"))
        );
    }
    
    @Test public void
    canCallClassConstructor() {
        VariableIdentifierNode reference = Nodes.id("Person");
        GlobalDeclaration declaration = globalDeclaration("Person");
        fixture.addReference(reference, declaration);
        
        StaticContext context = fixture.context();
        ClassType type = new ClassType(fullyQualifiedName("Person"));
        context.addClass(declaration, type, typeParameters(), ScalarTypeInfo.EMPTY);
        
        CallNode call = Nodes.call(reference);
        TypeResult<ValueInfo> result = inferType(call);
        assertThat(result, isType(type));
    }
    
    private TypeResult<ValueInfo> inferType(CallNode expression) {
        return fixture.get(CallExpressionTypeInferer.class).inferValueInfo(expression);
    }
    
    private Matcher<TypeResult<ValueInfo>> isType(Type type) {
        return isSuccessWithValue(ValueInfo.unassignableValue(type));
    }
}
