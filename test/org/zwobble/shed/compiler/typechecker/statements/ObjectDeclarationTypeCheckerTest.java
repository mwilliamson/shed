package org.zwobble.shed.compiler.typechecker.statements;

import org.junit.Test;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.ObjectDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeCheckerTestFixture;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typechecker.errors.CannotReturnHereError;
import org.zwobble.shed.compiler.typechecker.errors.TypeMismatchError;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.collect.ImmutableMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;

public class ObjectDeclarationTypeCheckerTest {
    private final TypeCheckerTestFixture fixture = TypeCheckerTestFixture.build();
    
    @Test public void
    bodyOfObjectIsTypeChecked() {
        VariableIdentifierNode stringReference = Nodes.id("String");
        fixture.addReference(stringReference, fixture.stringTypeDeclaration());
        ObjectDeclarationNode objectDeclarationNode = new ObjectDeclarationNode(
            "browser",
            Nodes.block(Nodes.immutableVar("version", stringReference, Nodes.number("1.2")))
        );
        TypeResult<?> result = typeCheckObjectDeclaration(objectDeclarationNode);
        assertThat(result, isFailureWithErrors(new TypeMismatchError(CoreTypes.STRING, CoreTypes.DOUBLE)));
    }
    
    @Test public void
    objectDeclarationDoesNotReturnFromScope() {
        ObjectDeclarationNode objectDeclarationNode = new ObjectDeclarationNode("browser", Nodes.block());
        TypeResult<StatementTypeCheckResult> result = typeCheckObjectDeclaration(objectDeclarationNode);
        assertThat(result, is(TypeResult.success(StatementTypeCheckResult.noReturn())));
    }
    
    @Test public void
    objectDeclarationBodyCannotReturn() {
        ObjectDeclarationNode objectDeclarationNode = new ObjectDeclarationNode(
            "browser",
            Nodes.block(Nodes.returnStatement(Nodes.number("42")))
        );
        TypeResult<StatementTypeCheckResult> result = typeCheckObjectDeclaration(objectDeclarationNode);
        assertThat(result, isFailureWithErrors(new CannotReturnHereError()));
    }

    @Test public void
    objectDeclarationCreatesNewTypeWithPublicMembers() {
        ObjectDeclarationNode objectDeclarationNode = 
            new ObjectDeclarationNode("browser", Nodes.block(
                Nodes.immutableVar("version", Nodes.number("1.2")),
                Nodes.publik(Nodes.immutableVar("name", Nodes.string("firefox")))
            ));
        fixture.addFullyQualifiedName(objectDeclarationNode, fullyQualifiedName("shed", "browser"));
        StaticContext staticContext = fixture.context();
        TypeResult<StatementTypeCheckResult> result = typeCheckObjectDeclaration(objectDeclarationNode);
        assertThat(result, is(TypeResult.success(StatementTypeCheckResult.noReturn())));
        ScalarType browserType = (ScalarType)staticContext.get(objectDeclarationNode).getType();
        assertThat(browserType.getFullyQualifiedName(), is(fullyQualifiedName("shed", "browser")));
        ScalarTypeInfo browserTypeInfo = staticContext.getInfo(browserType);
        assertThat(browserTypeInfo.getMembers(), is((Object)ImmutableMap.of("name", ValueInfo.unassignableValue(CoreTypes.STRING))));
    }
    
    private TypeResult<StatementTypeCheckResult> typeCheckObjectDeclaration(ObjectDeclarationNode objectDeclaration) {
        ObjectDeclarationTypeChecker typeChecker = fixture.get(ObjectDeclarationTypeChecker.class);
        return typeChecker.typeCheck(objectDeclaration, Option.<Type>none());
    }
}
