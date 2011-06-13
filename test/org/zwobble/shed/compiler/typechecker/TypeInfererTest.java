package org.zwobble.shed.compiler.typechecker;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.types.CoreTypes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.typechecker.TypeInferer.inferType;

public class TypeInfererTest {
    @Test public void
    canInferTypeOfBooleanLiteralsAsBoolean() {
        assertThat(inferType(new BooleanLiteralNode(true), null), is(CoreTypes.BOOLEAN));
        assertThat(inferType(new BooleanLiteralNode(false), null), is(CoreTypes.BOOLEAN));
    }
    
    @Test public void
    canInferTypeOfNumberLiteralsAsNumber() {
        assertThat(inferType(new NumberLiteralNode("2.2"), null), is(CoreTypes.NUMBER));
    }
    
    @Test public void
    canInferTypeOfStringLiteralsAsString() {
        assertThat(inferType(new StringLiteralNode("Everything's as if we never said"), null), is(CoreTypes.STRING));
    }
    
    @Test public void
    variableReferencesHaveTypeOfVariable() {
        StaticContext context = new StaticContext();
        context.add("value", CoreTypes.STRING);
        assertThat(inferType(new VariableIdentifierNode("value"), context), is(CoreTypes.STRING));
    }
}
