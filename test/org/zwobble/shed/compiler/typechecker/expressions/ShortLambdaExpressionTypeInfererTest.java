package org.zwobble.shed.compiler.typechecker.expressions;

import java.util.Collections;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.zwobble.shed.compiler.errors.CompilerErrorDescription;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.typechecker.TypeCheckerTestFixture;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typechecker.errors.TypeMismatchError;
import org.zwobble.shed.compiler.typechecker.errors.UntypedReferenceError;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.ScalarType;

import static org.zwobble.shed.compiler.errors.CompilerErrors.error;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.errorStrings;
import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;
import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.typechecker.TypeResultMatchers.isSuccessWithValue;

public class ShortLambdaExpressionTypeInfererTest {
    private final TypeCheckerTestFixture fixture = TypeCheckerTestFixture.build();

    private final VariableIdentifierNode doubleReference = fixture.doubleTypeReference();
    private final VariableIdentifierNode stringReference = fixture.stringTypeReference();

    @Test public void
    canInferTypeOfShortLambdaExpressionWithoutArgumentsNorExplicitReturnType() {
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            Collections.<FormalArgumentNode>emptyList(),
            none(ExpressionNode.class),
            new NumberLiteralNode("42")
        );
        TypeResult<ValueInfo> result = inferType(functionExpression);
        assertThat(result, isSuccessWithValue(ValueInfo.unassignableValue(CoreTypes.functionTypeOf(CoreTypes.DOUBLE))));
    }
    
    @Test public void
    errorIfCannotTypeBodyOfShortLambdaExpression() {
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            Collections.<FormalArgumentNode>emptyList(),
            none(ExpressionNode.class),
            new VariableIdentifierNode("blah")
        );
        TypeResult<ValueInfo> result = inferType(functionExpression);
        assertThat(errorStrings(result), is(asList("Could not determine type of reference: blah")));
    }
    
    @Test public void
    errorIfTypeSpecifierAndTypeBodyOfShortLambdaExpressionDoNotAgree() {
        NumberLiteralNode body = new NumberLiteralNode("42");
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            Collections.<FormalArgumentNode>emptyList(),
            some(stringReference),
            body
        );
        TypeResult<ValueInfo> result = inferType(functionExpression);
        assertThat(
            result.getErrors(),
            is(asList(error(body, new TypeMismatchError(CoreTypes.STRING, CoreTypes.DOUBLE))))
        );
    }
    
    @Test public void
    bodyOfShortLambdaExpressionCanBeSubTypeOfExplicitReturnType() {
        FormalArgumentNode argument = Nodes.formalArgument("value", fixture.implementingClassTypeReference());
        VariableIdentifierNode body = Nodes.id("value");
        fixture.addReference(body, argument);
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            asList(argument),
            some(fixture.interfaceTypeReference()),
            body
        );
        TypeResult<ValueInfo> result = inferType(functionExpression);
        ScalarType expectedType = CoreTypes.functionTypeOf(fixture.implementingClassType(), fixture.interfaceType());
        assertThat(result, isSuccessWithValue(ValueInfo.unassignableValue(expectedType)));
    }
    
    @Test public void
    errorIfCannotFindArgumentType() {
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            asList(
                new FormalArgumentNode("name", new VariableIdentifierNode("Name")),
                new FormalArgumentNode("age", doubleReference),
                new FormalArgumentNode("address", new VariableIdentifierNode("Address"))
            ),
            none(ExpressionNode.class),
            new BooleanLiteralNode(true)
        );
        TypeResult<ValueInfo> result = inferType(functionExpression);
        assertThat(result, isFailureWithErrors(new UntypedReferenceError("Name"), new UntypedReferenceError("Address")));
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test public void
    errorIfCannotFindReturnType() {
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            Collections.<FormalArgumentNode>emptyList(),
            some(new VariableIdentifierNode("String")),
            new NumberLiteralNode("42")
        );
        TypeResult<ValueInfo> result = inferType(functionExpression);
        assertThat(result, isFailureWithErrors((Matcher)hasItem((CompilerErrorDescription)new UntypedReferenceError("String"))));
    }
    
    @Test public void
    canInferTypesOfArgumentsOfShortLambdaExpression() {
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            asList(new FormalArgumentNode("name", stringReference), new FormalArgumentNode("age", doubleReference)),
            none(ExpressionNode.class),
            new BooleanLiteralNode(true)
        );
        TypeResult<ValueInfo> result = inferType(functionExpression);
        ScalarType expectedType = CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.DOUBLE, CoreTypes.BOOLEAN);
        assertThat(result, isSuccessWithValue(ValueInfo.unassignableValue(expectedType)));
    }
    
    @Test public void
    shortLambdaExpressionAddsArgumentsToFunctionScope() {
        FormalArgumentNode ageArgument = new FormalArgumentNode("age", doubleReference);
        VariableIdentifierNode ageReference = new VariableIdentifierNode("age");
        fixture.addReference(ageReference, ageArgument);
        
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            asList(
                new FormalArgumentNode("name", stringReference),
                ageArgument
            ),
            none(ExpressionNode.class),
            ageReference
        );
        TypeResult<ValueInfo> result = inferType(functionExpression);
        ScalarType expectedFunctionType = CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.DOUBLE, CoreTypes.DOUBLE);
        assertThat(result, isSuccessWithValue(ValueInfo.unassignableValue(expectedFunctionType)));
    }
    
    @Test public void
    shortLambdaExpressionHandlesUnrecognisedArgumentTypes() {
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            asList(
                new FormalArgumentNode("name", new VariableIdentifierNode("Strink")),
                new FormalArgumentNode("age", new VariableIdentifierNode("Numer"))
            ),
            none(ExpressionNode.class),
            new NumberLiteralNode("4")
        );
        TypeResult<ValueInfo> result = inferType(functionExpression);
        CompilerErrorDescription[] errorsArray = { new UntypedReferenceError("Strink"), new UntypedReferenceError("Numer") };
        assertThat(result, isFailureWithErrors(errorsArray));
    }
    
    @Test public void
    shortLambdaExpressionHandlesUnrecognisedUntypeableBodyWhenReturnTypeIsExplicit() {
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            asList(
                new FormalArgumentNode("age", doubleReference)
            ),
            some(doubleReference),
            new VariableIdentifierNode("blah")
        );
        TypeResult<ValueInfo> result = inferType(functionExpression);
        CompilerErrorDescription[] errorsArray = { new UntypedReferenceError("blah") };
        assertThat(result, isFailureWithErrors(errorsArray));
    }
    
    private TypeResult<ValueInfo> inferType(ShortLambdaExpressionNode expression) {
        return fixture.get(ShortLambdaExpressionTypeInferer.class).inferValueInfo(expression);
    }
}
