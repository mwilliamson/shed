package org.zwobble.shed.compiler.typechecker.statements;

import java.util.Collections;

import org.junit.Test;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.VariableDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeCheckerTestFixture;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typechecker.errors.TypeMismatchError;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.UnknownType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;
import static org.zwobble.shed.compiler.CompilerTesting.isSuccess;
import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration.globalDeclaration;
import static org.zwobble.shed.compiler.typechecker.TypeResultMatchers.isSuccessWithValue;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.assignableValue;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.Members.members;

public class VariableDeclarationTypeCheckerTest {
    private final TypeCheckerTestFixture fixture = TypeCheckerTestFixture.build();
    private final VariableIdentifierNode stringReference = fixture.stringTypeReference();

    @Test public void
    declaringVariableWithoutTypeSpecifierAddsVariableAsUnknownValue() {
        StaticContext staticContext = standardContext();
        VariableDeclarationNode variableNode = Nodes.immutableVar("dontFeelLike", Nodes.string("dancing"));
        
        assertThat(forwardDeclare(variableNode, staticContext), isSuccess());
        assertThat(staticContext.getTypeOf(variableNode).get(), instanceOf(UnknownType.class));
    }
    
    @Test public void
    declaringVariableWithTypeSpecifierAddsItToScopeDuringForwardDeclaration() {
        StaticContext staticContext = standardContext();
        VariableDeclarationNode variableNode = Nodes.immutableVar("dontFeelLike", stringReference, Nodes.string("dancing"));
        
        assertThat(forwardDeclare(variableNode, staticContext), isSuccess());
        assertThat(staticContext.getValueInfoFor(variableNode), is(some(ValueInfo.unassignableValue(CoreTypes.STRING))));
    }
    
    @Test public void
    declaringVariableWithoutTypeSpecifierAddsItToScopeDuringTypeCheck() {
        StaticContext staticContext = standardContext();
        VariableDeclarationNode variableNode = Nodes.immutableVar("x", Nodes.bool(true));
        
        assertThat(
            typeCheckVariableDeclaration(variableNode, staticContext),
            isSuccessWithValue(StatementTypeCheckResult.noReturn())
        );
        assertThat(staticContext.getValueInfoFor(variableNode), is(some(unassignableValue(CoreTypes.BOOLEAN))));
    }
    
    @Test public void
    declaringVariableWithTypeSpecifierDoesntAddItToScopeDuringTypeCheck() {
        StaticContext staticContext = standardContext();
        VariableDeclarationNode variableNode = Nodes.immutableVar("dontFeelLike", stringReference, Nodes.string("dancing"));
        
        assertThat(
            typeCheckVariableDeclaration(variableNode, staticContext),
            isSuccessWithValue(StatementTypeCheckResult.noReturn())
        );
        assertThat(staticContext.getValueInfoFor(variableNode), is(Option.<ValueInfo>none()));
    }
    
    @Test public void
    declaringMutableVariableAddsItToScopeAsAssignableValue() {
        StaticContext staticContext = standardContext();
        VariableDeclarationNode variableNode = Nodes.mutableVar("x", Nodes.bool(true));
        
        assertThat(
            typeCheckVariableDeclaration(variableNode, staticContext),
            isSuccessWithValue(StatementTypeCheckResult.noReturn())
        );
        assertThat(staticContext.getValueInfoFor(variableNode), is(some(assignableValue(CoreTypes.BOOLEAN))));
    }
    
    @Test public void
    errorsIfAttemptingToInitialiseAVariableWithExpressionOfWrongType() {
        StaticContext staticContext = standardContext();
        BooleanLiteralNode booleanNode = new BooleanLiteralNode(true);
        VariableDeclarationNode variableNode = Nodes.immutableVar("x", stringReference, booleanNode);
        
        forwardDeclare(variableNode, staticContext);
        assertThat(
            typeCheckVariableDeclaration(variableNode, staticContext),
            isFailureWithErrors(new TypeMismatchError(CoreTypes.STRING, CoreTypes.BOOLEAN))
        );
    }

    @Test public void
    canInstantiateVariableWithSubType() {
        VariableIdentifierNode iterableTypeReference = Nodes.id("Iterable");
        GlobalDeclaration iterableTypeDeclaration = globalDeclaration("Iterable");
        fixture.addReference(iterableTypeReference, iterableTypeDeclaration);

        VariableIdentifierNode listReference = Nodes.id("myList");
        GlobalDeclaration listDeclaration = globalDeclaration("myList");
        fixture.addReference(listReference, listDeclaration);
        
        StaticContext staticContext = standardContext();
        InterfaceType iterableType = new InterfaceType(fullyQualifiedName("shed", "util", "Iterable"));
        ClassType listType = new ClassType(fullyQualifiedName("shed", "util", "List"));
        ScalarTypeInfo listTypeInfo = new ScalarTypeInfo(interfaces(iterableType), members());
        staticContext.addClass(listDeclaration, listType, Collections.<Type>emptyList(), listTypeInfo);
        staticContext.addInterface(iterableTypeDeclaration, iterableType, ScalarTypeInfo.EMPTY);
        
        VariableDeclarationNode variableNode = Nodes.immutableVar("x", iterableTypeReference, listReference);
        
        assertThat(
            typeCheckVariableDeclaration(variableNode, staticContext),
            isSuccessWithValue(StatementTypeCheckResult.noReturn())
        );
    }
    
    private TypeResult<?> forwardDeclare(VariableDeclarationNode node, StaticContext context) {
        VariableDeclarationTypeChecker typeChecker = typeChecker(context);
        return typeChecker.forwardDeclare(node);
    }
    
    private TypeResult<StatementTypeCheckResult> typeCheckVariableDeclaration(VariableDeclarationNode node, StaticContext context) {
        VariableDeclarationTypeChecker typeChecker = typeChecker(context);
        return typeChecker.typeCheck(node, Option.<Type>none());
    }

    private VariableDeclarationTypeChecker typeChecker(StaticContext context) {
        return fixture.get(VariableDeclarationTypeChecker.class);
    }
    
    private StaticContext standardContext() {
        return fixture.context();
    }
}
