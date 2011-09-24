package org.zwobble.shed.compiler.typechecker;

import org.junit.Test;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.SimpleErrorDescription;
import org.zwobble.shed.compiler.naming.FullyQualifiedNamesBuilder;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.ObjectDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.referenceresolution.ReferencesBuilder;
import org.zwobble.shed.compiler.typechecker.errors.CannotReturnHereError;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.collect.ImmutableMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;

public class ObjectDeclarationTypeCheckerTest {
    private final SimpleNodeLocations nodeLocations = new SimpleNodeLocations();
    private final ReferencesBuilder references = new ReferencesBuilder();
    private final FullyQualifiedNamesBuilder fullNames = new FullyQualifiedNamesBuilder();
    
    private StaticContext staticContext() {
        return StaticContext.defaultContext(references.build(), fullNames.build());
    }
    
    @Test public void
    bodyOfObjectIsTypeChecked() {
        VariableIdentifierNode stringReference = Nodes.id("String");
        references.addReference(stringReference, CoreModule.GLOBAL_DECLARATIONS.get("String"));
        ObjectDeclarationNode objectDeclarationNode = new ObjectDeclarationNode(
            "browser",
            Nodes.block(Nodes.immutableVar("version", stringReference, Nodes.number("1.2")))
        );
        TypeResult<?> result = typeCheckObjectDeclaration(objectDeclarationNode, staticContext());
        assertThat(result, isFailureWithErrors(
            new SimpleErrorDescription("Cannot initialise variable of type \"String\" with expression of type \"Number\"")
        ));
    }
    
    @Test public void
    objectDeclarationDoesNotReturnFromScope() {
        ObjectDeclarationNode objectDeclarationNode = new ObjectDeclarationNode("browser", Nodes.block());
        TypeResult<StatementTypeCheckResult> result = typeCheckObjectDeclaration(objectDeclarationNode, staticContext());
        assertThat(result, is(TypeResult.success(StatementTypeCheckResult.noReturn())));
    }
    
    @Test public void
    objectDeclarationBodyCannotReturn() {
        ObjectDeclarationNode objectDeclarationNode = new ObjectDeclarationNode(
            "browser",
            Nodes.block(Nodes.returnStatement(Nodes.number("42")))
        );
        TypeResult<StatementTypeCheckResult> result = 
            typeCheckObjectDeclaration(objectDeclarationNode, staticContext());
        assertThat(result, isFailureWithErrors(new CannotReturnHereError()));
    }

    @Test public void
    objectDeclarationCreatesNewTypeWithPublicMembers() {
        ObjectDeclarationNode objectDeclarationNode = 
            new ObjectDeclarationNode("browser", Nodes.block(
                Nodes.immutableVar("version", Nodes.number("1.2")),
                Nodes.publik(Nodes.immutableVar("name", Nodes.string("firefox")))
            ));
        fullNames.addFullyQualifiedName(objectDeclarationNode, fullyQualifiedName("shed", "browser"));
        StaticContext staticContext = staticContext();
        TypeResult<StatementTypeCheckResult> result = 
            typeCheckObjectDeclaration(objectDeclarationNode, staticContext);
        assertThat(result, is(TypeResult.success(StatementTypeCheckResult.noReturn())));
        ScalarType browserType = (ScalarType)staticContext.get(objectDeclarationNode).getType();
        assertThat(browserType.getMembers(), is((Object)ImmutableMap.of("name", ValueInfo.unassignableValue(CoreTypes.STRING))));
        assertThat(browserType.getFullyQualifiedName(), is(fullyQualifiedName("shed", "browser")));
    }
    
    private TypeResult<StatementTypeCheckResult> typeCheckObjectDeclaration(
        ObjectDeclarationNode objectDeclaration, StaticContext staticContext
    ) {
        ObjectDeclarationTypeChecker typeChecker = TypeCheckerInjector.inject(ObjectDeclarationTypeChecker.class);
        return typeChecker.typeCheck(objectDeclaration, nodeLocations, staticContext, Option.<Type>none());
    }
}
