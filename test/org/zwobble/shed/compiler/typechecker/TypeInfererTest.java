package org.zwobble.shed.compiler.typechecker;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.CallNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.ImmutableVariableNode;
import org.zwobble.shed.compiler.parsing.nodes.LongLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.MemberAccessNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeApplicationNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.FormalTypeParameter;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.TypeApplication;
import org.zwobble.shed.compiler.types.TypeFunction;

import com.google.common.collect.ImmutableMap;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.errorStrings;
import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.parsing.SourcePosition.position;
import static org.zwobble.shed.compiler.parsing.SourceRange.range;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class TypeInfererTest {
    private final SimpleNodeLocations nodeLocations = new SimpleNodeLocations();
    
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
        VariableIdentifierNode node = new VariableIdentifierNode("value");
        nodeLocations.put(node, range(position(3, 5), position(7, 4)));
        TypeResult<Type> result = inferType(node, context);
        assertThat(result, is(
            (Object)failure(asList(new CompilerError(
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
            none(ExpressionNode.class),
            new NumberLiteralNode("42")
        );
        TypeResult<Type> result = inferType(functionExpression, context);
        assertThat(result, is((Object)success(
            new TypeApplication(CoreTypes.functionType(0), asList(CoreTypes.NUMBER))
        )));
    }
    
    @Test public void
    errorIfCannotTypeBodyOfShortLambdaExpression() {
        StaticContext context = new StaticContext();
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            Collections.<FormalArgumentNode>emptyList(),
            none(ExpressionNode.class),
            new VariableIdentifierNode("blah")
        );
        TypeResult<Type> result = inferType(functionExpression, context);
        assertThat(errorStrings(result), is(asList("No variable \"blah\" in scope")));
    }
    
    @Test public void
    errorIfTypeSpecifierAndTypeBodyOfShortLambdaExpressionDoNotAgree() {
        StaticContext context = new StaticContext();
        context.add("String", new TypeApplication(CoreTypes.CLASS, asList(CoreTypes.STRING)));
        NumberLiteralNode body = new NumberLiteralNode("42");
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            Collections.<FormalArgumentNode>emptyList(),
            some(new VariableIdentifierNode("String")),
            body
        );
        nodeLocations.put(body, range(position(3, 5), position(7, 4)));
        TypeResult<Type> result = inferType(functionExpression, context);
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
                new FormalArgumentNode("name", new VariableIdentifierNode("Name")),
                new FormalArgumentNode("age", new VariableIdentifierNode("Number")),
                new FormalArgumentNode("address", new VariableIdentifierNode("Address"))
            ),
            none(ExpressionNode.class),
            new BooleanLiteralNode(true)
        );
        TypeResult<Type> result = inferType(functionExpression, context);
        assertThat(errorStrings(result), is(asList("No variable \"Name\" in scope", "No variable \"Address\" in scope")));
    }
    
    @Test public void
    errorIfCannotFindReturnType() {
        StaticContext context = new StaticContext();
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            Collections.<FormalArgumentNode>emptyList(),
            some(new VariableIdentifierNode("String")),
            new NumberLiteralNode("42")
        );
        TypeResult<Type> result = inferType(functionExpression, context);
        assertThat(errorStrings(result), is(asList("No variable \"String\" in scope")));
    }
    
    @Test public void
    canInferTypesOfArgumentsOfShortLambdaExpression() {
        StaticContext context = new StaticContext();
        context.add("String", CoreTypes.classOf(CoreTypes.STRING));
        context.add("Number", CoreTypes.classOf(CoreTypes.NUMBER));
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            asList(
                new FormalArgumentNode("name", new VariableIdentifierNode("String")),
                new FormalArgumentNode("age", new VariableIdentifierNode("Number"))
            ),
            none(ExpressionNode.class),
            new BooleanLiteralNode(true)
        );
        TypeResult<Type> result = inferType(functionExpression, context);
        assertThat(result, is(success(
            (Type) new TypeApplication(CoreTypes.functionType(2), asList(CoreTypes.STRING, CoreTypes.NUMBER, CoreTypes.BOOLEAN))
        )));
    }
    
    @Test public void
    canFindTypeOfLongLambdaExpression() {
        StaticContext context = new StaticContext();
        context.add("String", CoreTypes.classOf(CoreTypes.STRING));
        context.add("Number", CoreTypes.classOf(CoreTypes.NUMBER));
        context.add("Boolean", CoreTypes.classOf(CoreTypes.BOOLEAN));
        LongLambdaExpressionNode functionExpression = new LongLambdaExpressionNode(
            asList(
                new FormalArgumentNode("name", new VariableIdentifierNode("String")),
                new FormalArgumentNode("age", new VariableIdentifierNode("Number"))
            ),
            new VariableIdentifierNode("Boolean"),
            asList((StatementNode)new ReturnNode(new BooleanLiteralNode(true)))
        );
        TypeResult<Type> result = inferType(functionExpression, context);
        assertThat(result, is(success(
            (Type) new TypeApplication(CoreTypes.functionType(2), asList(CoreTypes.STRING, CoreTypes.NUMBER, CoreTypes.BOOLEAN))
        )));
    }
    
    @Test public void
    bodyOfLongLambdaExpressionIsTypeChecked() {
        StaticContext context = new StaticContext();
        context.add("String", CoreTypes.classOf(CoreTypes.STRING));
        context.add("Number", CoreTypes.classOf(CoreTypes.NUMBER));
        context.add("Boolean", CoreTypes.classOf(CoreTypes.BOOLEAN));
        LongLambdaExpressionNode functionExpression = new LongLambdaExpressionNode(
            Collections.<FormalArgumentNode>emptyList(),
            new VariableIdentifierNode("Boolean"),
            asList(
                new ImmutableVariableNode(
                    "x",
                    some(new VariableIdentifierNode("String")),
                    new BooleanLiteralNode(true)
                ),
                new ReturnNode(new BooleanLiteralNode(true))
            )
        );
        TypeResult<Type> result = inferType(functionExpression, context);
        assertThat(errorStrings(result), is(asList("Cannot initialise variable of type \"String\" with expression of type \"Boolean\"")));
    }
    
    @Test public void
    bodyOfLongLambdaExpressionMustReturnExpressionOfTypeSpecifiedInSignature() {
        StaticContext context = new StaticContext();
        context.add("Boolean", CoreTypes.classOf(CoreTypes.BOOLEAN));
        LongLambdaExpressionNode functionExpression = new LongLambdaExpressionNode(
            Collections.<FormalArgumentNode>emptyList(),
            new VariableIdentifierNode("Boolean"),
            Arrays.<StatementNode>asList(
                new ReturnNode(new NumberLiteralNode("4.2"))
            )
        );
        TypeResult<Type> result = inferType(functionExpression, context);
        assertThat(errorStrings(result), is(asList("Expected return expression of type \"Boolean\" but was of type \"Number\"")));
    }
    
    @Test public void
    longLambdaExpressionAddsArgumentsToFunctionScope() {
        StaticContext context = new StaticContext();
        context.add("String", CoreTypes.classOf(CoreTypes.STRING));
        context.add("Number", CoreTypes.classOf(CoreTypes.NUMBER));
        LongLambdaExpressionNode functionExpression = new LongLambdaExpressionNode(
            asList(
                new FormalArgumentNode("name", new VariableIdentifierNode("String")),
                new FormalArgumentNode("age", new VariableIdentifierNode("Number"))
            ),
            new VariableIdentifierNode("Number"),
            asList((StatementNode)new ReturnNode(new VariableIdentifierNode("age")))
        );
        TypeResult<Type> result = inferType(functionExpression, context);
        assertThat(result, is(success(
            (Type) new TypeApplication(CoreTypes.functionType(2), asList(CoreTypes.STRING, CoreTypes.NUMBER, CoreTypes.NUMBER))
        )));
    }
    
    @Test public void
    longLambdaExpressionHandlesUnrecognisedArgumentTypes() {
        StaticContext context = new StaticContext();
        context.add("Number", CoreTypes.classOf(CoreTypes.NUMBER));
        LongLambdaExpressionNode functionExpression = new LongLambdaExpressionNode(
            asList(
                new FormalArgumentNode("name", new VariableIdentifierNode("Strink"))
            ),
            new VariableIdentifierNode("Number"),
            asList((StatementNode)new ReturnNode(new NumberLiteralNode("4")))
        );
        TypeResult<Type> result = inferType(functionExpression, context);
        assertThat(errorStrings(result), is(asList("No variable \"Strink\" in scope")));
    }
    
    @Test public void
    shortLambdaExpressionAddsArgumentsToFunctionScope() {
        StaticContext context = new StaticContext();
        context.add("String", CoreTypes.classOf(CoreTypes.STRING));
        context.add("Number", CoreTypes.classOf(CoreTypes.NUMBER));
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            asList(
                new FormalArgumentNode("name", new VariableIdentifierNode("String")),
                new FormalArgumentNode("age", new VariableIdentifierNode("Number"))
            ),
            none(ExpressionNode.class),
            new VariableIdentifierNode("age")
        );
        TypeResult<Type> result = inferType(functionExpression, context);
        assertThat(result, is(success(
            (Type) new TypeApplication(CoreTypes.functionType(2), asList(CoreTypes.STRING, CoreTypes.NUMBER, CoreTypes.NUMBER))
        )));
    }
    
    @Test public void
    shortLambdaExpressionHandlesUnrecognisedArgumentTypes() {
        StaticContext context = new StaticContext();
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            asList(
                new FormalArgumentNode("name", new VariableIdentifierNode("Strink")),
                new FormalArgumentNode("age", new VariableIdentifierNode("Numer"))
            ),
            none(ExpressionNode.class),
            new NumberLiteralNode("4")
        );
        TypeResult<Type> result = inferType(functionExpression, context);
        assertThat(errorStrings(result), is(asList("No variable \"Strink\" in scope", "No variable \"Numer\" in scope")));
    }
    
    @Test public void
    shortLambdaExpressionHandlesUnrecognisedUntypeableBodyWhenReturnTypeIsExplicit() {
        StaticContext context = new StaticContext();
        context.add("Number", CoreTypes.classOf(CoreTypes.NUMBER));
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            asList(
                new FormalArgumentNode("age", new VariableIdentifierNode("Number"))
            ),
            some(new VariableIdentifierNode("Number")),
            new VariableIdentifierNode("blah")
        );
        TypeResult<Type> result = inferType(functionExpression, context);
        assertThat(errorStrings(result), is(asList("No variable \"blah\" in scope")));
    }
    
    @Test public void
    shortLambdaExpressionCannotHaveTwoArgumentsWithSameName() {
        StaticContext context = new StaticContext();
        context.add("Number", CoreTypes.classOf(CoreTypes.NUMBER));
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            asList(
                new FormalArgumentNode("age", new VariableIdentifierNode("Number")),
                new FormalArgumentNode("age", new VariableIdentifierNode("Number"))
            ),
            none(ExpressionNode.class),
            new BooleanLiteralNode(true)
        );
        TypeResult<Type> result = inferType(functionExpression, context);
        assertThat(errorStrings(result), is(asList("Duplicate argument name \"age\"")));
    }
    
    @Test public void
    longLambdaExpressionCannotHaveTwoArgumentsWithSameName() {
        StaticContext context = new StaticContext();
        context.add("Number", CoreTypes.classOf(CoreTypes.NUMBER));
        LongLambdaExpressionNode functionExpression = new LongLambdaExpressionNode(
            asList(
                new FormalArgumentNode("age", new VariableIdentifierNode("Number")),
                new FormalArgumentNode("age", new VariableIdentifierNode("Number"))
            ),
            new VariableIdentifierNode("Number"),
            asList((StatementNode)new ReturnNode(new NumberLiteralNode("4")))
        );
        TypeResult<Type> result = inferType(functionExpression, context);
        assertThat(errorStrings(result), is(asList("Duplicate argument name \"age\"")));
    }
    
    @Test public void
    bodyOfLongLambdaExpressionMustReturn() {
        StaticContext context = new StaticContext();
        context.add("Boolean", CoreTypes.classOf(CoreTypes.BOOLEAN));
        LongLambdaExpressionNode functionExpression = new LongLambdaExpressionNode(
            Collections.<FormalArgumentNode>emptyList(),
            new VariableIdentifierNode("Boolean"),
            Collections.<StatementNode>emptyList()
        );
        TypeResult<Type> result = inferType(functionExpression, context);
        assertThat(errorStrings(result), is(asList("Expected return statement")));
    }
    
    @Test public void
    functionCallsHaveTypeOfReturnTypeOfFunctionWithNoArguments() {
        StaticContext context = new StaticContext();
        context.add("magic", new TypeApplication(CoreTypes.functionType(0), asList(CoreTypes.NUMBER)));
        CallNode call = Nodes.call(Nodes.id("magic"));
        TypeResult<Type> result = inferType(call, context);
        assertThat(result, is(success(CoreTypes.NUMBER)));
    }
    
    @Test public void
    functionCallsHaveTypeOfReturnTypeOfFunctionWithCorrectArguments() {
        StaticContext context = new StaticContext();
        // isLength: (String, Number) -> Boolean 
        context.add("isLength", new TypeApplication(CoreTypes.functionType(2), asList(CoreTypes.STRING, CoreTypes.NUMBER, CoreTypes.BOOLEAN)));
        CallNode call = Nodes.call(Nodes.id("isLength"), Nodes.string("Blah"), Nodes.number("4"));
        TypeResult<Type> result = inferType(call, context);
        assertThat(result, is(success(CoreTypes.BOOLEAN)));
    }
    
    @Test public void
    errorIfActualArgumentsAreNotAssignableToFormalArguments() {
        StaticContext context = new StaticContext();
        // isLength: (String, Number) -> Boolean 
        context.add("isLength", new TypeApplication(CoreTypes.functionType(2), asList(CoreTypes.STRING, CoreTypes.NUMBER, CoreTypes.BOOLEAN)));
        CallNode call = Nodes.call(Nodes.id("isLength"), Nodes.number("4"), Nodes.string("Blah"));
        TypeResult<Type> result = inferType(call, context);
        assertThat(
            errorStrings(result),
            is(asList(
                "Expected expression of type String as argument 1, but got expression of type Number",
                "Expected expression of type Number as argument 2, but got expression of type String"
            ))
        );
    }
    
    @Test public void
    cannotCallNonFunctionTypeApplications() {
        StaticContext context = new StaticContext();
        ClassType classType = new ClassType(asList("example"), "List", Collections.<InterfaceType>emptySet());
        TypeFunction typeFunction = new TypeFunction(classType, asList(new FormalTypeParameter("T")));
        context.add("isLength", new TypeApplication(typeFunction, asList(CoreTypes.STRING)));
        CallNode call = Nodes.call(Nodes.id("isLength"));
        TypeResult<Type> result = inferType(call, context);
        assertThat(
            errorStrings(result),
            is(asList(
                "Cannot call objects that aren't functions"
            ))
        );
    }
    
    @Test public void
    cannotCallTypesThatArentFunctionApplications() {
        StaticContext context = new StaticContext();
        context.add("isLength", CoreTypes.BOOLEAN);
        CallNode call = Nodes.call(Nodes.id("isLength"));
        TypeResult<Type> result = inferType(call, context);
        assertThat(
            errorStrings(result),
            is(asList(
                "Cannot call objects that aren't functions"
            ))
        );
    }
    
    @Test public void
    errorIfCallingFunctionWithWrongNumberOfArguments() {
        StaticContext context = new StaticContext();
        context.add("isLength", new TypeApplication(CoreTypes.functionType(2), asList(CoreTypes.STRING, CoreTypes.NUMBER, CoreTypes.BOOLEAN)));
        CallNode call = Nodes.call(Nodes.id("isLength"), Nodes.number("4"));
        TypeResult<Type> result = inferType(call, context);
        assertThat(
            errorStrings(result),
            is(asList("Function requires 2 argument(s), but is called with 1"))
        );
    }
    
    @Test public void
    memberAccessHasTypeOfMember() {
        StaticContext context = new StaticContext();
        InterfaceType interfaceType = new InterfaceType(
            asList("shed", "example"),
            "Brother",
            ImmutableMap.<String, Type>of("age", CoreTypes.NUMBER)
        );
        context.add("heAintHeavy", interfaceType);
        MemberAccessNode memberAccess = Nodes.member(Nodes.id("heAintHeavy"), "age");
        TypeResult<Type> result = inferType(memberAccess, context);
        assertThat(result, is(success(CoreTypes.NUMBER)));
    }
    
    @Test public void
    memberAccessFailsIfInterfaceDoesNotHaveSpecifiedMember() {
        StaticContext context = new StaticContext();
        InterfaceType interfaceType = new InterfaceType(
            asList("shed", "example"),
            "Brother",
            ImmutableMap.<String, Type>of("age", CoreTypes.NUMBER)
        );
        context.add("heAintHeavy", interfaceType);
        MemberAccessNode memberAccess = Nodes.member(Nodes.id("heAintHeavy"), "height");
        TypeResult<Type> result = inferType(memberAccess, context);
        assertThat(
            errorStrings(result),
            is(asList("No such member: height"))
        );
    }
    
    @Test public void
    applyingTypeUpdatesParameterisedTypeWithType() {
        StaticContext context = new StaticContext();
        FormalTypeParameter typeParameter = new FormalTypeParameter("T");
        TypeFunction listTypeFunction = new TypeFunction(
            new InterfaceType(asList("shed"), "List", ImmutableMap.<String, Type>of()),
            asList(typeParameter)
        );
        context.add("List", listTypeFunction);
        context.add("Number", CoreTypes.classOf(CoreTypes.NUMBER));
        TypeApplicationNode typeApplication = Nodes.typeApply(Nodes.id("List"), Nodes.id("Number"));
        
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            asList(new FormalArgumentNode("dummy", typeApplication)),
            none(ExpressionNode.class),
            new NumberLiteralNode("42")
        );
        TypeResult<Type> result = inferType(functionExpression, context);
        assertThat(result, is((Object)success(
            new TypeApplication(CoreTypes.functionType(1), asList(new TypeApplication(listTypeFunction, asList(CoreTypes.NUMBER)), CoreTypes.NUMBER))
        )));
    }
    
//    @Test public void
//    applyingTypeUpdatesFunctionArgumentAndReturnTypes() {
//        StaticContext context = new StaticContext();
//        FormalTypeParameter typeParameter = new FormalTypeParameter("T");
//        context.add("identity", new TypeFunction(
//            TypeApplication.build(
//                CoreTypes.functionType(1),
//                Arrays.<Type>asList(typeParameter, typeParameter)
//            ),
//            asList(typeParameter)
//        ));
//        CallNode call = Nodes.call(Nodes.typeApply(Nodes.id("identity"), Nodes.number("2")));
//        TypeResult<Type> result = inferType(call, context);
//        assertThat(result, is(success(CoreTypes.NUMBER)));
//    }
    
    private TypeResult<Type> inferType(ExpressionNode expression, StaticContext context) {
        return TypeInferer.inferType(expression, nodeLocations, context);
    }
}
