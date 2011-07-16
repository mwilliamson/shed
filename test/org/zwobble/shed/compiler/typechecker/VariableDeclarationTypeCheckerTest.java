package org.zwobble.shed.compiler.typechecker;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ImmutableVariableNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeIdentifierNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeReferenceNode;
import org.zwobble.shed.compiler.types.CoreTypes;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.errorStrings;
import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.parsing.SourcePosition.position;
import static org.zwobble.shed.compiler.parsing.SourceRange.range;
import static org.zwobble.shed.compiler.typechecker.VariableDeclarationTypeChecker.typeCheckImmutableVariableDeclaration;

public class VariableDeclarationTypeCheckerTest {
    private final SimpleNodeLocations nodeLocations = new SimpleNodeLocations();
    private final StaticContext staticContext = new StaticContext();

    @Test public void
    declaringVariableAddsItToScope() {
        ImmutableVariableNode variableNode = new ImmutableVariableNode(
            "x",
            none(TypeReferenceNode.class),
            new BooleanLiteralNode(true)
        );
        
        staticContext.add("Boolean", CoreTypes.classOf(CoreTypes.BOOLEAN));
        assertThat(
            typeCheckImmutableVariableDeclaration(variableNode, nodeLocations, staticContext),
            is(TypeResult.<Void>success(null))
        );
        assertThat(staticContext.get("x"), is(some(CoreTypes.BOOLEAN)));
    }
    
    @Test public void
    errorsIfAttemptingToInitialiseAVariableWithExpressionOfWrongType() {
        BooleanLiteralNode booleanNode = new BooleanLiteralNode(true);
        ImmutableVariableNode variableNode = new ImmutableVariableNode(
            "x",
            some((TypeReferenceNode)new TypeIdentifierNode("String")),
            booleanNode
        );
        nodeLocations.put(booleanNode, range(position(4, 12), position(6, 6)));
        
        staticContext.add("String", CoreTypes.classOf(CoreTypes.STRING));
        assertThat(
            typeCheckImmutableVariableDeclaration(variableNode, nodeLocations, staticContext),
            is(TypeResult.<Void>failure(asList(new CompilerError(
                range(position(4, 12), position(6, 6)),
                "Cannot initialise variable of type \"String\" with expression of type \"Boolean\""
            ))))
        );
    }
    
    @Test public void
    errorsIfDeclaringVariableWithNameAlreadyDeclaredInSameScope() {
        ImmutableVariableNode variableNode = new ImmutableVariableNode("x", none(TypeReferenceNode.class), new BooleanLiteralNode(true));
        nodeLocations.put(variableNode, range(position(4, 12), position(6, 6)));
        
        staticContext.add("x", CoreTypes.BOOLEAN);
        
        assertThat(
            errorStrings(typeCheckImmutableVariableDeclaration(variableNode, nodeLocations, staticContext)),
            is(asList("The variable \"x\" has already been declared in this scope"))
        );
    }
}
