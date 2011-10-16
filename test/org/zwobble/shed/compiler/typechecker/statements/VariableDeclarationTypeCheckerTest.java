package org.zwobble.shed.compiler.typechecker.statements;

import org.junit.Test;
import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.naming.FullyQualifiedNamesBuilder;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.VariableDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.referenceresolution.ReferencesBuilder;
import org.zwobble.shed.compiler.typechecker.SimpleNodeLocations;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeCheckerInjector;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typechecker.VariableLookupResult;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import com.google.inject.Injector;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.isSuccess;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.parsing.SourcePosition.position;
import static org.zwobble.shed.compiler.parsing.SourceRange.range;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.assignableValue;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.Members.members;

public class VariableDeclarationTypeCheckerTest {
    private final SimpleNodeLocations nodeLocations = new SimpleNodeLocations();
    private final ReferencesBuilder references = new ReferencesBuilder();

    private final GlobalDeclarationNode stringDeclaration = new GlobalDeclarationNode("String");
    private final VariableIdentifierNode stringReference = new VariableIdentifierNode("String");

    @Test public void
    declaringVariableWithoutTypeSpecifierAddsVariableAsUnknownValue() {
        StaticContext staticContext = standardContext();
        VariableDeclarationNode variableNode = Nodes.immutableVar("dontFeelLike", Nodes.string("dancing"));
        
        assertThat(forwardDeclare(variableNode, nodeLocations, staticContext), isSuccess());
        assertThat(staticContext.get(variableNode), is(VariableLookupResult.success(ValueInfo.unknown())));
    }
    
    @Test public void
    declaringVariableWithTypeSpecifierAddsItToScopeDuringForwardDeclaration() {
        StaticContext staticContext = standardContext();
        VariableDeclarationNode variableNode = Nodes.immutableVar("dontFeelLike", stringReference, Nodes.string("dancing"));
        
        assertThat(forwardDeclare(variableNode, nodeLocations, staticContext), isSuccess());
        assertThat(staticContext.get(variableNode), is(VariableLookupResult.success(ValueInfo.unassignableValue(CoreTypes.STRING))));
    }
    
    @Test public void
    declaringVariableWithoutTypeSpecifierAddsItToScopeDuringTypeCheck() {
        StaticContext staticContext = standardContext();
        VariableDeclarationNode variableNode = Nodes.immutableVar("x", Nodes.bool(true));
        
        assertThat(
            typeCheckVariableDeclaration(variableNode, nodeLocations, staticContext),
            is(TypeResult.success(StatementTypeCheckResult.noReturn()))
        );
        assertThat(staticContext.get(variableNode), is(VariableLookupResult.success(unassignableValue(CoreTypes.BOOLEAN))));
    }
    
    @Test public void
    declaringVariableWithTypeSpecifierDoesntAddItToScopeDuringTypeCheck() {
        StaticContext staticContext = standardContext();
        VariableDeclarationNode variableNode = Nodes.immutableVar("dontFeelLike", stringReference, Nodes.string("dancing"));
        
        assertThat(
            typeCheckVariableDeclaration(variableNode, nodeLocations, staticContext),
            is(TypeResult.success(StatementTypeCheckResult.noReturn()))
        );
        assertThat(staticContext.get(variableNode), is(VariableLookupResult.notDeclared()));
    }
    
    @Test public void
    declaringMutableVariableAddsItToScopeAsAssignableValue() {
        StaticContext staticContext = standardContext();
        VariableDeclarationNode variableNode = Nodes.mutableVar("x", Nodes.bool(true));
        
        assertThat(
            typeCheckVariableDeclaration(variableNode, nodeLocations, staticContext),
            is(TypeResult.success(StatementTypeCheckResult.noReturn()))
        );
        assertThat(staticContext.get(variableNode), is(VariableLookupResult.success(assignableValue(CoreTypes.BOOLEAN))));
    }
    
    @Test public void
    errorsIfAttemptingToInitialiseAVariableWithExpressionOfWrongType() {
        StaticContext staticContext = standardContext();
        BooleanLiteralNode booleanNode = new BooleanLiteralNode(true);
        VariableDeclarationNode variableNode = Nodes.immutableVar("x", stringReference, booleanNode);
        nodeLocations.put(booleanNode, range(position(4, 12), position(6, 6)));
        
        forwardDeclare(variableNode, nodeLocations, staticContext);
        assertThat(
            typeCheckVariableDeclaration(variableNode, nodeLocations, staticContext),
            is(TypeResult.<StatementTypeCheckResult>failure(asList(CompilerError.error(
                range(position(4, 12), position(6, 6)),
                "Cannot initialise variable of type \"String\" with expression of type \"Boolean\""
            ))))
        );
    }

    @Test public void
    canInstantiateVariableWithSubType() {
        VariableIdentifierNode iterableTypeReference = Nodes.id("Iterable");
        GlobalDeclarationNode iterableTypeDeclaration = new GlobalDeclarationNode("Iterable");
        references.addReference(iterableTypeReference, iterableTypeDeclaration);

        VariableIdentifierNode listReference = Nodes.id("myList");
        GlobalDeclarationNode listDeclaration = new GlobalDeclarationNode("myList");
        references.addReference(listReference, listDeclaration);
        
        StaticContext staticContext = standardContext();
        InterfaceType iterableType = new InterfaceType(fullyQualifiedName("shed", "util", "Iterable"));
        ClassType listType = new ClassType(fullyQualifiedName("shed", "util", "List"));
        ScalarTypeInfo listTypeInfo = new ScalarTypeInfo(interfaces(iterableType), members());
        staticContext.add(listDeclaration, unassignableValue(listType));
        staticContext.add(iterableTypeDeclaration, unassignableValue(CoreTypes.classOf(iterableType)));
        staticContext.addInfo(listType, listTypeInfo);
        
        VariableDeclarationNode variableNode = Nodes.immutableVar("x", iterableTypeReference, listReference);
        
        assertThat(
            typeCheckVariableDeclaration(variableNode, nodeLocations, staticContext),
            is(TypeResult.success(StatementTypeCheckResult.noReturn()))
        );
    }
    
    private TypeResult<?> forwardDeclare(
        VariableDeclarationNode node, NodeLocations nodeLocations, StaticContext context
    ) {
        VariableDeclarationTypeChecker typeChecker = typeChecker(nodeLocations);
        return typeChecker.forwardDeclare(node, context);
    }
    
    private TypeResult<StatementTypeCheckResult> typeCheckVariableDeclaration(
        VariableDeclarationNode node, NodeLocations nodeLocations, StaticContext context
    ) {
        VariableDeclarationTypeChecker typeChecker = typeChecker(nodeLocations);
        return typeChecker.typeCheck(node, context, Option.<Type>none());
    }

    private VariableDeclarationTypeChecker typeChecker(NodeLocations nodeLocations) {
        Injector injector = TypeCheckerInjector.build(nodeLocations, new FullyQualifiedNamesBuilder().build());
        VariableDeclarationTypeChecker typeChecker = injector.getInstance(VariableDeclarationTypeChecker.class);
        return typeChecker;
    }
    
    private StaticContext standardContext() {
        references.addReference(stringReference, stringDeclaration);
        
        StaticContext context = new StaticContext(references.build());
        context.add(stringDeclaration, unassignableValue(CoreTypes.classOf(CoreTypes.STRING)));
        
        return context;
    }
}
