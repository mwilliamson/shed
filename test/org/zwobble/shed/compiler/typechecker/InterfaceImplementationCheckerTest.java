package org.zwobble.shed.compiler.typechecker;

import org.junit.Test;
import org.zwobble.shed.compiler.errors.HasErrors;
import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.parsing.nodes.ClassDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Declaration;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.typechecker.errors.MissingMemberError;
import org.zwobble.shed.compiler.typechecker.errors.WrongMemberTypeError;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;
import static org.zwobble.shed.compiler.CompilerTesting.isSuccess;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration.globalDeclaration;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
import static org.zwobble.shed.compiler.types.CoreTypes.functionTypeOf;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.Members.members;

public class InterfaceImplementationCheckerTest {
    private final TypeCheckerTestFixture fixture = TypeCheckerTestFixture.build();
    private final FullyQualifiedName fullyQualifiedName = fullyQualifiedName("shed", "Account");
    private final ClassType type = new ClassType(fullyQualifiedName);
    private final ClassDeclarationNode declaration = Nodes.clazz("Account", Nodes.noFormalArguments(), Nodes.block());
    private final InterfaceType interfaceType = new InterfaceType(fullyQualifiedName("Store"));
    private final Declaration interfaceDeclaration = globalDeclaration("Store");
    private final StaticContext context = fixture.context();

    @Test public void
    classImplementsInterfaceIfAllMembersAreSubtypesOfInterfacesMembers() {
        ScalarType interfaceFunctionType = functionTypeOf(fixture.implementingClassType(), CoreTypes.UNIT);
        ScalarTypeInfo interfaceTypeInfo = new ScalarTypeInfo(interfaces(), members("add", unassignableValue(interfaceFunctionType)));
        context.addInterface(interfaceDeclaration, interfaceType, interfaceTypeInfo);
        ScalarType subClassFunctionType = functionTypeOf(fixture.interfaceType(), CoreTypes.UNIT);
        context.addInfo(type, new ScalarTypeInfo(interfaces(interfaceType), members("add", unassignableValue(subClassFunctionType))));

        HasErrors result = check(declaration, type);
        
        assertThat(result, isSuccess());
    }
    
    @Test public void
    typeErrorIfClassDoesNotImplementAllMembersOfInterface() {
        ScalarType interfaceFunctionType = functionTypeOf(fixture.implementingClassType(), CoreTypes.UNIT);
        ScalarTypeInfo interfaceTypeInfo = new ScalarTypeInfo(interfaces(), members("add", unassignableValue(interfaceFunctionType)));
        context.addInterface(interfaceDeclaration, interfaceType, interfaceTypeInfo);
        context.addInfo(type, new ScalarTypeInfo(interfaces(interfaceType), members()));

        HasErrors result = check(declaration, type);
        
        assertThat(result, isFailureWithErrors(new MissingMemberError(interfaceType, "add")));
    }
    
    @Test public void
    typeErrorIfClassDoesNotImplementMemberWithCorrectType() {
        ScalarType interfaceFunctionType = functionTypeOf(fixture.interfaceType(), CoreTypes.UNIT);
        ScalarTypeInfo interfaceTypeInfo = new ScalarTypeInfo(interfaces(), members("add", unassignableValue(interfaceFunctionType)));
        context.addInterface(interfaceDeclaration, interfaceType, interfaceTypeInfo);
        ScalarType subClassFunctionType = functionTypeOf(fixture.implementingClassType(), CoreTypes.UNIT);
        context.addInfo(type, new ScalarTypeInfo(interfaces(interfaceType), members("add", unassignableValue(subClassFunctionType))));

        HasErrors result = check(declaration, type);
        
        assertThat(result, isFailureWithErrors(new WrongMemberTypeError(
            interfaceType,
            "add",
            interfaceFunctionType,
            functionTypeOf(fixture.implementingClassType(), CoreTypes.UNIT)
        )));
    }
    
    private HasErrors check(SyntaxNode declaration, ScalarType type) {
        return fixture.get(InterfaceImplementationChecker.class).checkInterfaces(declaration, type);
    }
}
