package org.zwobble.shed.compiler.typechecker.statements;

import org.junit.Test;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.parsing.nodes.InterfaceDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.typechecker.ShedTypeValue;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeCheckerTestFixture;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.zwobble.shed.compiler.CompilerTesting.isSuccess;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.typechecker.TypeResultMatchers.isSuccessWithValue;
import static org.zwobble.shed.compiler.types.Member.member;

public class InterfaceDeclarationTypeCheckerTest {
    private final TypeCheckerTestFixture fixture = TypeCheckerTestFixture.build();
    private final FullyQualifiedName fullyQualifiedName = fullyQualifiedName("shed", "Browser");
    private final InterfaceType type = new InterfaceType(fullyQualifiedName);
    
    @Test public void
    interfaceTypeIsBuiltInForwardDeclarationWithMembers() {
        InterfaceDeclarationNode declaration = Nodes.interfaceDeclaration("Browser", Nodes.interfaceBody(
            Nodes.funcSignature("alert", asList(Nodes.formalArgument("string", fixture.stringTypeReference())), fixture.unitTypeReference())
        ));
        fixture.addType(declaration, type);
        StaticContext context = fixture.context();
        
        TypeResult<?> result = forwardDeclare(declaration);
        
        assertThat(result, isSuccess());
        ShedTypeValue value = (ShedTypeValue) context.get(declaration).getValue().get();
        InterfaceType type = (InterfaceType)value.getType();
        ScalarTypeInfo typeInfo = context.getInfo(type);
        assertThat(typeInfo.getMembers(), contains(
            member("alert", ValueInfo.unassignableValue(CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.UNIT)))
        ));
    }
    
    @Test public void
    interfaceDeclarationDoesntReturn() {
        InterfaceDeclarationNode declaration = Nodes.interfaceDeclaration("Browser", Nodes.interfaceBody());
        fixture.addType(declaration, type);
        
        TypeResult<StatementTypeCheckResult> result = typeCheck(declaration);
        
        assertThat(result, isSuccessWithValue(StatementTypeCheckResult.noReturn()));
    }
    
    private TypeResult<?> forwardDeclare(InterfaceDeclarationNode interfaceDeclaration) {
        return typeChecker().forwardDeclare(interfaceDeclaration);
    }
    
    private TypeResult<StatementTypeCheckResult> typeCheck(InterfaceDeclarationNode interfaceDeclaration) {
        return typeChecker().typeCheck(interfaceDeclaration, Option.<Type>none());
    }

    private InterfaceDeclarationTypeChecker typeChecker() {
        return fixture.get(InterfaceDeclarationTypeChecker.class);
    }

}
