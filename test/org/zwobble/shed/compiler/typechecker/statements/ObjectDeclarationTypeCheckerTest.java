package org.zwobble.shed.compiler.typechecker.statements;

import org.junit.Test;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.Declaration;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.ObjectDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeCheckerTestFixture;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typechecker.errors.CannotReturnHereError;
import org.zwobble.shed.compiler.typechecker.errors.MissingMemberError;
import org.zwobble.shed.compiler.typechecker.errors.TypeMismatchError;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.Interfaces;
import org.zwobble.shed.compiler.types.Members;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration.globalDeclaration;
import static org.zwobble.shed.compiler.typechecker.TypeResultMatchers.isSuccessWithValue;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
import static org.zwobble.shed.compiler.types.CoreTypes.functionTypeOf;
import static org.zwobble.shed.compiler.types.Members.members;

public class ObjectDeclarationTypeCheckerTest {
    private final TypeCheckerTestFixture fixture = TypeCheckerTestFixture.build();
    
    @Test public void
    bodyOfObjectIsTypeChecked() {
        VariableIdentifierNode stringReference = Nodes.id("String");
        fixture.addReference(stringReference, fixture.stringTypeDeclaration());
        ObjectDeclarationNode objectDeclarationNode = Nodes.object(
            "browser",
            Nodes.block(Nodes.immutableVar("version", stringReference, Nodes.number("1.2")))
        );
        TypeResult<?> result = typeCheckObjectDeclaration(objectDeclarationNode);
        assertThat(result, isFailureWithErrors(new TypeMismatchError(CoreTypes.STRING, CoreTypes.DOUBLE)));
    }
    
    @Test public void
    objectDeclarationDoesNotReturnFromScope() {
        ObjectDeclarationNode objectDeclarationNode = Nodes.object("browser", Nodes.block());
        TypeResult<StatementTypeCheckResult> result = typeCheckObjectDeclaration(objectDeclarationNode);
        assertThat(result, isSuccessWithValue(StatementTypeCheckResult.noReturn()));
    }
    
    @Test public void
    objectDeclarationBodyCannotReturn() {
        ObjectDeclarationNode objectDeclarationNode = Nodes.object(
            "browser",
            Nodes.block(Nodes.returnStatement(Nodes.number("42")))
        );
        TypeResult<StatementTypeCheckResult> result = typeCheckObjectDeclaration(objectDeclarationNode);
        assertThat(result, isFailureWithErrors(new CannotReturnHereError()));
    }

    @Test public void
    objectDeclarationCreatesNewTypeWithPublicMembers() {
        ObjectDeclarationNode objectDeclarationNode = 
            Nodes.object("browser", Nodes.block(
                Nodes.immutableVar("version", Nodes.number("1.2")),
                Nodes.publik(Nodes.immutableVar("name", Nodes.string("firefox")))
            ));
        InterfaceType type = new InterfaceType(fullyQualifiedName("shed", "browser"));
        fixture.addType(objectDeclarationNode, type);
        StaticContext staticContext = fixture.context();
        TypeResult<StatementTypeCheckResult> result = typeCheckObjectDeclaration(objectDeclarationNode);
        assertThat(result, isSuccessWithValue(StatementTypeCheckResult.noReturn()));
        assertThat(staticContext.getTypeOf(objectDeclarationNode).get(), is((Type)type));
        ScalarTypeInfo browserTypeInfo = staticContext.getInfo(type);
        assertThat(browserTypeInfo.getMembers(), is(members("name", ValueInfo.unassignableValue(CoreTypes.STRING))));
    }
    
    @Test public void
    superTypesOfObjectAreChecked() {
        InterfaceType superType = new InterfaceType(fullyQualifiedName("Store"));
        VariableIdentifierNode interfaceReference = Nodes.id("Store");
        Declaration interfaceDeclaration = globalDeclaration("Store");
        fixture.addReference(interfaceReference, interfaceDeclaration);
        
        ObjectDeclarationNode objectDeclarationNode = Nodes.object("browser", Nodes.expressions(interfaceReference), Nodes.block());
        InterfaceType objectType = new InterfaceType(fullyQualifiedName("shed", "browser"));
        fixture.addType(objectDeclarationNode, objectType);
        StaticContext staticContext = fixture.context();
        Members interfaceMembers = members("add", unassignableValue(functionTypeOf(CoreTypes.STRING, CoreTypes.UNIT)));
        staticContext.addInterface(interfaceDeclaration, superType, new ScalarTypeInfo(Interfaces.interfaces(), interfaceMembers));
        
        TypeResult<StatementTypeCheckResult> result = typeCheckObjectDeclaration(objectDeclarationNode);
        
        assertThat(result, isFailureWithErrors(new MissingMemberError(superType, "add")));
    }
    
    private TypeResult<StatementTypeCheckResult> typeCheckObjectDeclaration(ObjectDeclarationNode objectDeclaration) {
        ObjectDeclarationTypeChecker typeChecker = fixture.get(ObjectDeclarationTypeChecker.class);
        return typeChecker.typeCheck(objectDeclaration, Option.<Type>none());
    }
}
