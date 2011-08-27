package org.zwobble.shed.compiler.typechecker;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
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
import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.typechecker.ReturnStatementTypeChecker.typeCheckReturnStatement;

public class ReturnStatementTypeCheckerTest {
    private final SimpleNodeLocations nodeLocations = new SimpleNodeLocations();
    private final StaticContext staticContext = new StaticContext();
    
    @Test public void
    cannotReturnFromTopLevel() {
        ReturnNode returnStatement = new ReturnNode(new BooleanLiteralNode(true));
        assertThat(
            errorStrings(typeCheckReturnStatement(returnStatement, nodeLocations, staticContext)),
            is(asList("Cannot return from this scope"))
        );
    }
    
    @Test public void
    returnExpressionIsTypeChecked() {
        ReturnNode returnStatement = new ReturnNode(new VariableIdentifierNode("x"));
        staticContext.enterNewScope(some(CoreTypes.STRING));
        assertThat(
            errorStrings(typeCheckReturnStatement(returnStatement, nodeLocations, staticContext)),
            is(asList("No variable \"x\" in scope"))
        );
    }
    
    @Test public void
    returnExpressionCanBeSubTypeOfReturnType() {
        ReturnNode returnStatement = new ReturnNode(new VariableIdentifierNode("x"));
        InterfaceType iterableType = new InterfaceType(asList("shed", "util"), "Iterable", ImmutableMap.<String, Type>of());
        ClassType listType = new ClassType(asList("shed", "util"), "List", newHashSet(iterableType));
        staticContext.enterNewScope(some((Type)iterableType));
        staticContext.add("x", listType);
        assertThat(
            typeCheckReturnStatement(returnStatement, nodeLocations, staticContext),
            is(TypeResult.<Void>success(null))
        );
    }
}
