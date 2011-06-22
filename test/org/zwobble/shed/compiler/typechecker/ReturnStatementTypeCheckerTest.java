package org.zwobble.shed.compiler.typechecker;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.types.CoreTypes;

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
}
