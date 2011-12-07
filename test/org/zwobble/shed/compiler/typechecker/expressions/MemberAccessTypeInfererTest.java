package org.zwobble.shed.compiler.typechecker.expressions;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration;
import org.zwobble.shed.compiler.parsing.nodes.MemberAccessNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeCheckerTestFixture;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typechecker.errors.NotScalarTypeError;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.Types;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.errorStrings;
import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration.globalDeclaration;
import static org.zwobble.shed.compiler.typechecker.TypeResultMatchers.isSuccessWithValue;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.assignableValue;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.Members.members;

public class MemberAccessTypeInfererTest {
    private final TypeCheckerTestFixture fixture = TypeCheckerTestFixture.build();
    private final StaticContext context = fixture.context();
    private final VariableIdentifierNode reference = Nodes.id("heAintHeavy");
    private final GlobalDeclaration declaration = globalDeclaration("heAintHeavy");

    public MemberAccessTypeInfererTest() {
        fixture.addReference(reference, declaration);
    }
    
    @Test public void
    memberAccessHasTypeOfMember() {
        InterfaceType interfaceType = new InterfaceType(fullyQualifiedName("shed", "example", "Brother"));
        context.add(declaration, unassignableValue(interfaceType));
        context.addInfo(interfaceType, new ScalarTypeInfo(interfaces(), members("age", unassignableValue(CoreTypes.DOUBLE))));
        
        MemberAccessNode memberAccess = Nodes.member(reference, "age");
        TypeResult<ValueInfo> result = inferValueInfo(memberAccess);
        assertThat(result, isSuccessWithValue(unassignableValue(CoreTypes.DOUBLE)));
    }
    
    @Test public void
    memberAccessIsAssignableIfMemberIsAssignable() {
        InterfaceType interfaceType = new InterfaceType(fullyQualifiedName("shed", "example", "Brother"));
        context.add(declaration, unassignableValue(interfaceType));
        context.addInfo(interfaceType, new ScalarTypeInfo(interfaces(), members("age", assignableValue(CoreTypes.DOUBLE))));
        
        MemberAccessNode memberAccess = Nodes.member(reference, "age");
        TypeResult<ValueInfo> result = inferValueInfo(memberAccess);
        assertThat(result, isSuccessWithValue(assignableValue(CoreTypes.DOUBLE)));
    }
    
    @Test public void
    memberAccessFailsIfInterfaceDoesNotHaveSpecifiedMember() {
        InterfaceType interfaceType = new InterfaceType(fullyQualifiedName("shed", "example", "Brother"));
        context.add(declaration, unassignableValue(interfaceType));
        context.addInfo(interfaceType, new ScalarTypeInfo(interfaces(), members("age", unassignableValue(CoreTypes.DOUBLE))));
        MemberAccessNode memberAccess = Nodes.member(reference, "height");
        TypeResult<ValueInfo> result = inferValueInfo(memberAccess);
        assertThat(
            errorStrings(result),
            is(asList("No such member: height"))
        );
    }
    
    @Test public void
    memberAccessFailsIfLeftHandExpressionIsNotScalarType() {
        Type type = Types.newUnknown();
        context.add(declaration, unassignableValue(type));
        MemberAccessNode memberAccess = Nodes.member(reference, "height");
        TypeResult<ValueInfo> result = inferValueInfo(memberAccess);
        assertThat(result, isFailureWithErrors(new NotScalarTypeError(type)));
    }
    
    private TypeResult<ValueInfo> inferValueInfo(MemberAccessNode expression) {
        return fixture.get(MemberAccessTypeInferer.class).inferValueInfo(expression);
    }
}
