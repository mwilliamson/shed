package org.zwobble.shed.compiler.typechecker.expressions;

import java.util.Collections;

import org.junit.Test;
import org.zwobble.shed.compiler.errors.CompilerErrorDescription;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.LongLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.typechecker.TypeCheckerTestFixture;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typechecker.errors.TypeMismatchError;
import org.zwobble.shed.compiler.typechecker.errors.UntypedReferenceError;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.ScalarType;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.errorStrings;
import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;
import static org.zwobble.shed.compiler.typechecker.TypeResultMatchers.isSuccessWithValue;

public class LongLambdaExpressionTypeInfererTest {
    private final TypeCheckerTestFixture fixture = TypeCheckerTestFixture.build();

    private final VariableIdentifierNode doubleReference = fixture.doubleTypeReference();
    private final VariableIdentifierNode stringReference = fixture.stringTypeReference();
    private final VariableIdentifierNode booleanReference = fixture.booleanTypeReference();

    
    @Test public void
    canFindTypeOfLongLambdaExpression() {
        LongLambdaExpressionNode functionExpression = new LongLambdaExpressionNode(
            asList(
                new FormalArgumentNode("name", stringReference),
                new FormalArgumentNode("age", doubleReference)
            ),
            booleanReference,
            Nodes.block(new ReturnNode(new BooleanLiteralNode(true)))
        );
        TypeResult<ValueInfo> result = inferType(functionExpression);
        ScalarType expectedFunctionType = CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.DOUBLE, CoreTypes.BOOLEAN);
        assertThat(result, isSuccessWithValue(ValueInfo.unassignableValue(expectedFunctionType)));
    }
    
    @Test public void
    bodyOfLongLambdaExpressionIsTypeChecked() {
        LongLambdaExpressionNode functionExpression = new LongLambdaExpressionNode(
            Collections.<FormalArgumentNode>emptyList(),
            booleanReference,
            Nodes.block(
                Nodes.immutableVar("x", stringReference, Nodes.bool(true)),
                new ReturnNode(new BooleanLiteralNode(true))
            )
        );
        TypeResult<ValueInfo> result = inferType(functionExpression);
        assertThat(result, isFailureWithErrors(new TypeMismatchError(CoreTypes.STRING, CoreTypes.BOOLEAN)));
    }
    
    @Test public void
    bodyOfLongLambdaExpressionMustReturnExpressionOfTypeSpecifiedInSignature() {
        LongLambdaExpressionNode functionExpression = new LongLambdaExpressionNode(
            Collections.<FormalArgumentNode>emptyList(),
            booleanReference,
            Nodes.block(
                new ReturnNode(new NumberLiteralNode("4.2"))
            )
        );
        TypeResult<ValueInfo> result = inferType(functionExpression);
        assertThat(errorStrings(result), is(asList("Expected return expression of type \"Boolean\" but was of type \"Double\"")));
    }
    
    @Test public void
    longLambdaExpressionAddsArgumentsToFunctionScope() {
        FormalArgumentNode ageArgument = new FormalArgumentNode("age", doubleReference);
        VariableIdentifierNode ageReference = new VariableIdentifierNode("age");
        fixture.addReference(ageReference, ageArgument);
        
        LongLambdaExpressionNode functionExpression = new LongLambdaExpressionNode(
            asList(
                new FormalArgumentNode("name", stringReference),
                ageArgument
            ),
            doubleReference,
            Nodes.block(new ReturnNode(ageReference))
        );
        TypeResult<ValueInfo> result = inferType(functionExpression);
        ScalarType expectedFunctionType = CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.DOUBLE, CoreTypes.DOUBLE);
        assertThat(result, isSuccessWithValue(ValueInfo.unassignableValue(expectedFunctionType)));
    }
    
    @Test public void
    longLambdaExpressionHandlesUnrecognisedArgumentTypes() {
        LongLambdaExpressionNode functionExpression = new LongLambdaExpressionNode(
            asList(
                new FormalArgumentNode("name", new VariableIdentifierNode("Strink"))
            ),
            doubleReference,
            Nodes.block(new ReturnNode(new NumberLiteralNode("4")))
        );
        TypeResult<ValueInfo> result = inferType(functionExpression);
        CompilerErrorDescription[] errorsArray = { new UntypedReferenceError("Strink") };
        assertThat(result, isFailureWithErrors(errorsArray));
    }
    
    @Test public void
    bodyOfLongLambdaExpressionMustReturn() {
        LongLambdaExpressionNode functionExpression = new LongLambdaExpressionNode(
            Collections.<FormalArgumentNode>emptyList(),
            booleanReference,
            Nodes.block()
        );
        TypeResult<ValueInfo> result = inferType(functionExpression);
        assertThat(errorStrings(result), is(asList("Expected return statement")));
    }
    
    private TypeResult<ValueInfo> inferType(LongLambdaExpressionNode expression) {
        return fixture.get(LongLambdaExpressionTypeInferer.class).inferValueInfo(expression);
    }
}
