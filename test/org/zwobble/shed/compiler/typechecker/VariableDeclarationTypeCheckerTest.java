package org.zwobble.shed.compiler.typechecker;

import org.junit.Test;
import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ImmutableVariableNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.referenceresolution.ReferencesBuilder;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.collect.ImmutableMap;

import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.parsing.SourcePosition.position;
import static org.zwobble.shed.compiler.parsing.SourceRange.range;
import static org.zwobble.shed.compiler.typechecker.VariableDeclarationTypeChecker.typeCheckVariableDeclaration;

public class VariableDeclarationTypeCheckerTest {
    private final SimpleNodeLocations nodeLocations = new SimpleNodeLocations();
    private final ReferencesBuilder references = new ReferencesBuilder();

    private final GlobalDeclarationNode stringDeclaration = new GlobalDeclarationNode("String");
    private final VariableIdentifierNode stringReference = new VariableIdentifierNode("String");

    @Test public void
    declaringVariableAddsItToScope() {
        StaticContext staticContext = standardContext();
        ImmutableVariableNode variableNode = new ImmutableVariableNode(
            "x",
            none(ExpressionNode.class),
            new BooleanLiteralNode(true)
        );
        
        assertThat(
            typeCheckVariableDeclaration(variableNode, nodeLocations, staticContext),
            is(TypeResult.success(StatementTypeCheckResult.noReturn()))
        );
        assertThat(staticContext.get(variableNode), is(VariableLookupResult.success(unassignableValue(CoreTypes.BOOLEAN))));
    }
    
    @Test public void
    errorsIfAttemptingToInitialiseAVariableWithExpressionOfWrongType() {
        StaticContext staticContext = standardContext();
        BooleanLiteralNode booleanNode = new BooleanLiteralNode(true);
        ImmutableVariableNode variableNode = new ImmutableVariableNode(
            "x",
            some(stringReference),
            booleanNode
        );
        nodeLocations.put(booleanNode, range(position(4, 12), position(6, 6)));
        
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
        InterfaceType iterableType = new InterfaceType(asList("shed", "util"), "Iterable", ImmutableMap.<String, Type>of());
        ClassType listType = new ClassType(asList("shed", "util"), "List", newHashSet(iterableType), ImmutableMap.<String, Type>of());
        staticContext.add(listDeclaration, unassignableValue(listType));
        staticContext.add(iterableTypeDeclaration, unassignableValue(CoreTypes.classOf(iterableType)));
        
        ImmutableVariableNode variableNode = new ImmutableVariableNode(
            "x",
            some(iterableTypeReference),
            listReference
        );
        
        assertThat(
            typeCheckVariableDeclaration(variableNode, nodeLocations, staticContext),
            is(TypeResult.success(StatementTypeCheckResult.noReturn()))
        );
    }
    
    private StaticContext standardContext() {
        references.addReference(stringReference, stringDeclaration);
        
        StaticContext context = new StaticContext(references.build());
        context.add(stringDeclaration, unassignableValue(CoreTypes.classOf(CoreTypes.STRING)));
        
        return context;
    }
}
