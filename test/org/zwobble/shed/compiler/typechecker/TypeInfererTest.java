package org.zwobble.shed.compiler.typechecker;

import java.util.Collections;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeIdentifierNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeReferenceNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.TypeApplication;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.errorStrings;
import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.parsing.SourcePosition.position;
import static org.zwobble.shed.compiler.parsing.SourceRange.range;
import static org.zwobble.shed.compiler.typechecker.TypeInferer.inferType;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class TypeInfererTest {
    private final SimpleNodeLocations nodeLocations = new SimpleNodeLocations();
    
    @Test public void
    canInferTypeOfBooleanLiteralsAsBoolean() {
        assertThat(inferType(new BooleanLiteralNode(true), nodeLocations, null), is(success(CoreTypes.BOOLEAN)));
        assertThat(inferType(new BooleanLiteralNode(false), nodeLocations, null), is(success(CoreTypes.BOOLEAN)));
    }
    
    @Test public void
    canInferTypeOfNumberLiteralsAsNumber() {
        assertThat(inferType(new NumberLiteralNode("2.2"), nodeLocations, null), is(success(CoreTypes.NUMBER)));
    }
    
    @Test public void
    canInferTypeOfStringLiteralsAsString() {
        assertThat(inferType(new StringLiteralNode("Everything's as if we never said"), nodeLocations, null), is(success(CoreTypes.STRING)));
    }
    
    @Test public void
    variableReferencesHaveTypeOfVariable() {
        StaticContext context = new StaticContext();
        context.add("value", CoreTypes.STRING);
        assertThat(inferType(new VariableIdentifierNode("value"), nodeLocations, context), is(success(CoreTypes.STRING)));
    }
    
    @Test public void
    cannotReferToVariableNotInContext() {
        StaticContext context = new StaticContext();
        VariableIdentifierNode node = new VariableIdentifierNode("value");
        nodeLocations.put(node, range(position(3, 5), position(7, 4)));
        TypeResult result = inferType(node, nodeLocations, context);
        assertThat(result, is(
            failure(asList(new CompilerError(
                range(position(3, 5), position(7, 4)),
                "No variable \"value\" in scope"
            )))
        ));
    }
    
    @Test public void
    canInferTypeOfShortLambdaExpressionWithoutArgumentsNorExplicitReturnType() {
        StaticContext context = new StaticContext();
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            Collections.<FormalArgumentNode>emptyList(),
            none(TypeReferenceNode.class),
            new NumberLiteralNode("42")
        );
        TypeResult result = inferType(functionExpression, nodeLocations, context);
        assertThat(result, is(success(
            new TypeApplication(CoreTypes.functionType(0), asList(CoreTypes.NUMBER))
        )));
    }
    
    @Test public void
    errorIfCannotTypeBodyOfShortLambdaExpression() {
        StaticContext context = new StaticContext();
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            Collections.<FormalArgumentNode>emptyList(),
            none(TypeReferenceNode.class),
            new VariableIdentifierNode("blah")
        );
        TypeResult result = inferType(functionExpression, nodeLocations, context);
        assertThat(errorStrings(result), is(asList("No variable \"blah\" in scope")));
    }
    
    @Test public void
    errorIfTypeSpecifierAndTypeBodyOfShortLambdaExpressionDoNotAgree() {
        StaticContext context = new StaticContext();
        context.add("String", new TypeApplication(CoreTypes.CLASS, asList(CoreTypes.STRING)));
        NumberLiteralNode body = new NumberLiteralNode("42");
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            Collections.<FormalArgumentNode>emptyList(),
            some((TypeReferenceNode)new TypeIdentifierNode("String")),
            body
        );
        nodeLocations.put(body, range(position(3, 5), position(7, 4)));
        TypeResult result = inferType(functionExpression, nodeLocations, context);
        assertThat(
            result.getErrors(),
            is(asList(new CompilerError(
                range(position(3, 5), position(7, 4)),
                "Type mismatch: expected expression of type \"String\" but was of type \"Number\""
            )))
        );
    }
    
    @Test public void
    errorIfCannotFindArgumentType() {
        StaticContext context = new StaticContext();
        context.add("Number", CoreTypes.classOf(CoreTypes.NUMBER));
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            asList(
                new FormalArgumentNode("name", new TypeIdentifierNode("Name")),
                new FormalArgumentNode("age", new TypeIdentifierNode("Number")),
                new FormalArgumentNode("address", new TypeIdentifierNode("Address"))
            ),
            none(TypeReferenceNode.class),
            new BooleanLiteralNode(true)
        );
        TypeResult result = inferType(functionExpression, nodeLocations, context);
        assertThat(errorStrings(result), is(asList("No variable \"Name\" in scope", "No variable \"Address\" in scope")));
    }
    
    @Test public void
    errorIfCannotFindReturnType() {
        StaticContext context = new StaticContext();
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            Collections.<FormalArgumentNode>emptyList(),
            some((TypeReferenceNode)new TypeIdentifierNode("String")),
            new NumberLiteralNode("42")
        );
        TypeResult result = inferType(functionExpression, nodeLocations, context);
        assertThat(errorStrings(result), is(asList("No variable \"String\" in scope")));
    }
    
    @Test public void
    canInferTypesOfArgumentsOfShortLambdaExpression() {
        StaticContext context = new StaticContext();
        context.add("String", CoreTypes.classOf(CoreTypes.STRING));
        context.add("Number", CoreTypes.classOf(CoreTypes.NUMBER));
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            asList(
                new FormalArgumentNode("name", new TypeIdentifierNode("String")),
                new FormalArgumentNode("age", new TypeIdentifierNode("Number"))
            ),
            none(TypeReferenceNode.class),
            new BooleanLiteralNode(true)
        );
        TypeResult result = inferType(functionExpression, nodeLocations, context);
        assertThat(result, is(success(
            (Type) new TypeApplication(CoreTypes.functionType(2), asList(CoreTypes.STRING, CoreTypes.NUMBER, CoreTypes.BOOLEAN))
        )));
    }
}
