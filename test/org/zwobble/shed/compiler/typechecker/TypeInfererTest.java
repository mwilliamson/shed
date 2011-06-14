package org.zwobble.shed.compiler.typechecker;

import java.util.Collections;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.Result;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionNode;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.errorStrings;
import static org.zwobble.shed.compiler.parsing.Result.success;
import static org.zwobble.shed.compiler.typechecker.TypeInferer.inferType;

public class TypeInfererTest {
    @Test public void
    canInferTypeOfBooleanLiteralsAsBoolean() {
        assertThat(inferType(new BooleanLiteralNode(true), null), is(success(CoreTypes.BOOLEAN)));
        assertThat(inferType(new BooleanLiteralNode(false), null), is(success(CoreTypes.BOOLEAN)));
    }
    
    @Test public void
    canInferTypeOfNumberLiteralsAsNumber() {
        assertThat(inferType(new NumberLiteralNode("2.2"), null), is(success(CoreTypes.NUMBER)));
    }
    
    @Test public void
    canInferTypeOfStringLiteralsAsString() {
        assertThat(inferType(new StringLiteralNode("Everything's as if we never said"), null), is(success(CoreTypes.STRING)));
    }
    
    @Test public void
    variableReferencesHaveTypeOfVariable() {
        StaticContext context = new StaticContext();
        context.add("value", CoreTypes.STRING);
        assertThat(inferType(new VariableIdentifierNode("value"), context), is(success(CoreTypes.STRING)));
    }
    
    @Test public void
    cannotReferToVariableNotInContext() {
        StaticContext context = new StaticContext();
        Result<Type> result = inferType(new VariableIdentifierNode("value"), context);
        assertThat(result.isFatal(), is(true));
        assertThat(errorStrings(result), is(asList("No variable \"value\" in scope")));
    }
    
    @Test public void
    canInferTypeOfFunctionLiteralWithNoArgumentsAndUnitReturnType() {
        StaticContext context = new StaticContext();
        FunctionNode functionExpression = new FunctionNode(
            Collections.<FormalArgumentNode>emptyList(),
            Collections.<StatementNode>emptyList()
        );
        Result<Type> result = inferType(functionExpression, context);
        assertThat(result, is(success((Type)CoreTypes.functionType())));
    }
}
