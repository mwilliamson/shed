package org.zwobble.shed.compiler.typechecker;

import org.junit.Test;
import org.zwobble.shed.compiler.SimpleCompilerError;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.ImmutableVariableNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementTypeCheckResult;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.collect.ImmutableMap;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.errorStrings;
import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.parsing.SourcePosition.position;
import static org.zwobble.shed.compiler.parsing.SourceRange.range;
import static org.zwobble.shed.compiler.typechecker.VariableDeclarationTypeChecker.typeCheckVariableDeclaration;

public class VariableDeclarationTypeCheckerTest {
    private final SimpleNodeLocations nodeLocations = new SimpleNodeLocations();
    private final StaticContext staticContext = new StaticContext();

    @Test public void
    declaringVariableAddsItToScope() {
        ImmutableVariableNode variableNode = new ImmutableVariableNode(
            "x",
            none(ExpressionNode.class),
            new BooleanLiteralNode(true)
        );
        
        staticContext.add("Boolean", CoreTypes.classOf(CoreTypes.BOOLEAN));
        assertThat(
            typeCheckVariableDeclaration(variableNode, nodeLocations, staticContext),
            is(TypeResult.success(StatementTypeCheckResult.noReturn()))
        );
        assertThat(staticContext.get("x"), is(VariableLookupResult.success(CoreTypes.BOOLEAN)));
    }
    
    @Test public void
    errorsIfAttemptingToInitialiseAVariableWithExpressionOfWrongType() {
        BooleanLiteralNode booleanNode = new BooleanLiteralNode(true);
        ImmutableVariableNode variableNode = new ImmutableVariableNode(
            "x",
            some(new VariableIdentifierNode("String")),
            booleanNode
        );
        nodeLocations.put(booleanNode, range(position(4, 12), position(6, 6)));
        
        staticContext.add("String", CoreTypes.classOf(CoreTypes.STRING));
        assertThat(
            typeCheckVariableDeclaration(variableNode, nodeLocations, staticContext),
            is(TypeResult.<StatementTypeCheckResult>failure(asList(new SimpleCompilerError(
                range(position(4, 12), position(6, 6)),
                "Cannot initialise variable of type \"String\" with expression of type \"Boolean\""
            ))))
        );
    }

    @Test public void
    canInstantiateVariableWithSubType() {
        InterfaceType iterableType = new InterfaceType(asList("shed", "util"), "Iterable", ImmutableMap.<String, Type>of());
        ClassType listType = new ClassType(asList("shed", "util"), "List", newHashSet(iterableType), ImmutableMap.<String, Type>of());
        staticContext.add("myList", listType);
        staticContext.add("Iterable", CoreTypes.classOf(iterableType));
        
        ImmutableVariableNode variableNode = new ImmutableVariableNode(
            "x",
            some(new VariableIdentifierNode("Iterable")),
            new VariableIdentifierNode("myList")
        );
        
        assertThat(
            typeCheckVariableDeclaration(variableNode, nodeLocations, staticContext),
            is(TypeResult.success(StatementTypeCheckResult.noReturn()))
        );
    }
    
    @Test public void
    errorsIfDeclaringVariableWithNameAlreadyDeclaredInSameScope() {
        ImmutableVariableNode variableNode = new ImmutableVariableNode("x", none(ExpressionNode.class), new BooleanLiteralNode(true));
        nodeLocations.put(variableNode, range(position(4, 12), position(6, 6)));
        
        staticContext.add("x", CoreTypes.BOOLEAN);
        
        assertThat(
            errorStrings(typeCheckVariableDeclaration(variableNode, nodeLocations, staticContext)),
            is(asList("The variable \"x\" has already been declared in this scope"))
        );
    }
}
