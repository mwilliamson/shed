package org.zwobble.shed.compiler.typechecker.expressions;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.zwobble.shed.compiler.errors.CompilerErrorDescription;
import org.zwobble.shed.compiler.parsing.nodes.AssignmentExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeCheckerTestFixture;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typechecker.errors.InvalidAssignmentError;
import org.zwobble.shed.compiler.typechecker.errors.TypeMismatchError;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.Interfaces;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration.globalDeclaration;
import static org.zwobble.shed.compiler.typechecker.TypeResultMatchers.isSuccessWithValue;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.assignableValue;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
import static org.zwobble.shed.compiler.types.Members.members;

public class AssignmentExpressionTypeInfererTest {
    private final TypeCheckerTestFixture fixture = TypeCheckerTestFixture.build();
    
    @Test public void
    assignmentHasTypeOfAssignedValue() {
        VariableIdentifierNode reference = Nodes.id("x");
        GlobalDeclaration declaration = globalDeclaration("x");
        fixture.addReference(reference, declaration);
        
        StaticContext context = fixture.context();
        
        context.add(declaration, assignableValue(CoreTypes.DOUBLE));
        
        TypeResult<ValueInfo> result = inferType(Nodes.assign(reference, Nodes.number("4")));
        assertThat(result, isType(CoreTypes.DOUBLE));
    }
    
    @Test public void
    cannotAssignToUnassignableValue() {
        VariableIdentifierNode reference = Nodes.id("x");
        GlobalDeclaration declaration = globalDeclaration("x");
        fixture.addReference(reference, declaration);
        
        StaticContext context = fixture.context();
        
        context.add(declaration, unassignableValue(CoreTypes.DOUBLE));
        
        TypeResult<ValueInfo> result = inferType(Nodes.assign(reference, Nodes.number("4")));
        CompilerErrorDescription[] errorsArray = { new InvalidAssignmentError() };
        assertThat(result, isFailureWithErrors(errorsArray));
    }
    
    @Test public void
    cannotAssignValueIfNotSubTypeOfVariableType() {
        VariableIdentifierNode reference = Nodes.id("x");
        GlobalDeclaration declaration = globalDeclaration("x");
        fixture.addReference(reference, declaration);
        
        StaticContext context = fixture.context();
        
        context.add(declaration, assignableValue(CoreTypes.DOUBLE));
        
        TypeResult<ValueInfo> result = inferType(Nodes.assign(reference, Nodes.bool(true)));
        CompilerErrorDescription[] errorsArray = { new TypeMismatchError(CoreTypes.DOUBLE, CoreTypes.BOOLEAN) };
        assertThat(result, isFailureWithErrors(errorsArray));
    }
    
    @Test public void
    canAssignValueIfSubTypeOfVariableType() {
        VariableIdentifierNode interfaceReference = Nodes.id("iterable");
        GlobalDeclaration interfaceDeclaration = globalDeclaration("iterable");
        fixture.addReference(interfaceReference, interfaceDeclaration);

        VariableIdentifierNode classReference = Nodes.id("iterable");
        GlobalDeclaration classDeclaration = globalDeclaration("iterable");
        fixture.addReference(classReference, classDeclaration);
        
        StaticContext context = fixture.context();
        
        InterfaceType interfaceType = new InterfaceType(fullyQualifiedName("shed", "Iterable"));
        ClassType classType = new ClassType(fullyQualifiedName("shed", "List"));
        context.add(interfaceDeclaration, assignableValue(interfaceType));
        context.add(classDeclaration, assignableValue(classType));
        context.addInfo(classType, new ScalarTypeInfo(Interfaces.interfaces(interfaceType), members()));
        
        TypeResult<ValueInfo> result = inferType(Nodes.assign(interfaceReference, classReference));
        assertThat(result, isType(classType));
    }
    
    private TypeResult<ValueInfo> inferType(AssignmentExpressionNode expression) {
        return fixture.get(AssignmentExpressionTypeInferer.class).inferValueInfo(expression);
    }
    
    private Matcher<TypeResult<ValueInfo>> isType(Type type) {
        return isSuccessWithValue(ValueInfo.unassignableValue(type));
    }
}
