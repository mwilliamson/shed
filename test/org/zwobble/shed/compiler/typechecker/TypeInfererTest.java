package org.zwobble.shed.compiler.typechecker;

import java.util.Collections;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.naming.FullyQualifiedNamesBuilder;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.CallNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.LongLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.MemberAccessNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeApplicationNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.referenceresolution.ReferencesBuilder;
import org.zwobble.shed.compiler.typechecker.errors.InvalidAssignmentError;
import org.zwobble.shed.compiler.typechecker.errors.TypeMismatchError;
import org.zwobble.shed.compiler.typechecker.errors.UntypedReferenceError;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.FormalTypeParameter;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.Interfaces;
import org.zwobble.shed.compiler.types.ParameterisedFunctionType;
import org.zwobble.shed.compiler.types.ParameterisedType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.TypeApplication;

import com.google.inject.Injector;

import static org.zwobble.shed.compiler.types.ParameterisedType.parameterisedType;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.errorStrings;
import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.parsing.SourcePosition.position;
import static org.zwobble.shed.compiler.parsing.SourceRange.range;
import static org.zwobble.shed.compiler.typechecker.TypeCheckerTesting.isFailureWithErrors;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.assignableValue;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.Members.members;

public class TypeInfererTest {
    private final SimpleNodeLocations nodeLocations = new SimpleNodeLocations();
    private final ReferencesBuilder references = new ReferencesBuilder();

    private final GlobalDeclarationNode numberDeclaration = new GlobalDeclarationNode("Number");
    private final VariableIdentifierNode numberReference = new VariableIdentifierNode("Number");
    
    private final GlobalDeclarationNode stringDeclaration = new GlobalDeclarationNode("String");
    private final VariableIdentifierNode stringReference = new VariableIdentifierNode("String");

    private final GlobalDeclarationNode booleanDeclaration = new GlobalDeclarationNode("Boolean");
    private final VariableIdentifierNode booleanReference = new VariableIdentifierNode("Boolean");
    
    @Test public void
    canInferTypeOfBooleanLiteralsAsBoolean() {
        assertThat(inferType(new BooleanLiteralNode(true), null), isType(CoreTypes.BOOLEAN));
        assertThat(inferType(new BooleanLiteralNode(false), null), isType(CoreTypes.BOOLEAN));
    }
    
    @Test public void
    canInferTypeOfNumberLiteralsAsNumber() {
        assertThat(inferType(new NumberLiteralNode("2.2"), null), isType(CoreTypes.NUMBER));
    }
    
    @Test public void
    canInferTypeOfStringLiteralsAsString() {
        assertThat(inferType(new StringLiteralNode("Everything's as if we never said"), null), isType(CoreTypes.STRING));
    }
    
    @Test public void
    canInferTypeOfUnitLiteralsAsUnit() {
        assertThat(inferType(Nodes.unit(), null), isType(CoreTypes.UNIT));
    }
    
    @Test public void
    variableReferencesHaveTypeOfVariable() {
        VariableIdentifierNode reference = new VariableIdentifierNode("value");
        GlobalDeclarationNode declaration = new GlobalDeclarationNode("value");
        references.addReference(reference, declaration);
        StaticContext context = blankContext();
        context.add(declaration, unassignableValue(CoreTypes.STRING));
        assertThat(inferType(reference, context), isType(CoreTypes.STRING));
    }
    
    @Test public void
    cannotReferToVariableNotInContext() {
        VariableIdentifierNode node = new VariableIdentifierNode("value");
        nodeLocations.put(node, range(position(3, 5), position(7, 4)));
        TypeResult<Type> result = inferType(node, blankContext());
        assertThat(result, is(
            (Object)failure(asList(new CompilerError(
                range(position(3, 5), position(7, 4)),
                new UntypedReferenceError("value")
            )))
        ));
    }
    
    @Test public void
    canInferTypeOfShortLambdaExpressionWithoutArgumentsNorExplicitReturnType() {
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            Collections.<FormalArgumentNode>emptyList(),
            none(ExpressionNode.class),
            new NumberLiteralNode("42")
        );
        TypeResult<Type> result = inferType(functionExpression, blankContext());
        assertThat(result, is(success(CoreTypes.functionTypeOf(CoreTypes.NUMBER))));
    }
    
    @Test public void
    errorIfCannotTypeBodyOfShortLambdaExpression() {
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            Collections.<FormalArgumentNode>emptyList(),
            none(ExpressionNode.class),
            new VariableIdentifierNode("blah")
        );
        TypeResult<Type> result = inferType(functionExpression, blankContext());
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
        nodeLocations.put(body, range(position(3, 5), position(7, 4)));
        TypeResult<Type> result = inferType(functionExpression, standardContext());
        assertThat(
            result.getErrors(),
            is(asList(new CompilerError(
                range(position(3, 5), position(7, 4)),
                new TypeMismatchError(CoreTypes.STRING, CoreTypes.NUMBER)
            )))
        );
    }
    
    @Test public void
    errorIfCannotFindArgumentType() {
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            asList(
                new FormalArgumentNode("name", new VariableIdentifierNode("Name")),
                new FormalArgumentNode("age", numberReference),
                new FormalArgumentNode("address", new VariableIdentifierNode("Address"))
            ),
            none(ExpressionNode.class),
            new BooleanLiteralNode(true)
        );
        TypeResult<Type> result = inferType(functionExpression, standardContext());
        assertThat(result, isFailureWithErrors(
            new UntypedReferenceError("Name"),
            new UntypedReferenceError("Address")
        ));
    }
    
    @Test public void
    errorIfCannotFindReturnType() {
        StaticContext context = blankContext();
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            Collections.<FormalArgumentNode>emptyList(),
            some(new VariableIdentifierNode("String")),
            new NumberLiteralNode("42")
        );
        TypeResult<Type> result = inferType(functionExpression, context);
        assertThat(result, isFailureWithErrors(new UntypedReferenceError("String")));
    }
    
    @Test public void
    canInferTypesOfArgumentsOfShortLambdaExpression() {
        GlobalDeclarationNode numberDeclaration = new GlobalDeclarationNode("Number");
        VariableIdentifierNode numberReference = new VariableIdentifierNode("Number");
        references.addReference(numberReference, numberDeclaration);
        
        GlobalDeclarationNode stringDeclaration = new GlobalDeclarationNode("String");
        VariableIdentifierNode stringReference = new VariableIdentifierNode("String");
        references.addReference(stringReference, stringDeclaration);
        
        StaticContext context = blankContext();
        context.add(numberDeclaration, unassignableValue(CoreTypes.classOf(CoreTypes.NUMBER)));
        context.add(stringDeclaration, unassignableValue(CoreTypes.classOf(CoreTypes.STRING)));
        
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            asList(new FormalArgumentNode("name", stringReference), new FormalArgumentNode("age", numberReference)),
            none(ExpressionNode.class),
            new BooleanLiteralNode(true)
        );
        TypeResult<Type> result = inferType(functionExpression, context);
        assertThat(result, is(success(CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.NUMBER, CoreTypes.BOOLEAN))));
    }
    
    @Test public void
    canFindTypeOfLongLambdaExpression() {
        LongLambdaExpressionNode functionExpression = new LongLambdaExpressionNode(
            asList(
                new FormalArgumentNode("name", stringReference),
                new FormalArgumentNode("age", numberReference)
            ),
            booleanReference,
            Nodes.block(new ReturnNode(new BooleanLiteralNode(true)))
        );
        TypeResult<Type> result = inferType(functionExpression, standardContext());
        assertThat(result, is(success(CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.NUMBER, CoreTypes.BOOLEAN))));
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
        TypeResult<Type> result = inferType(functionExpression, standardContext());
        assertThat(errorStrings(result), is(asList("Cannot initialise variable of type \"String\" with expression of type \"Boolean\"")));
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
        TypeResult<Type> result = inferType(functionExpression, standardContext());
        assertThat(errorStrings(result), is(asList("Expected return expression of type \"Boolean\" but was of type \"Number\"")));
    }
    
    @Test public void
    longLambdaExpressionAddsArgumentsToFunctionScope() {
        FormalArgumentNode ageArgument = new FormalArgumentNode("age", numberReference);
        VariableIdentifierNode ageReference = new VariableIdentifierNode("age");
        references.addReference(ageReference, ageArgument);
        
        LongLambdaExpressionNode functionExpression = new LongLambdaExpressionNode(
            asList(
                new FormalArgumentNode("name", stringReference),
                ageArgument
            ),
            numberReference,
            Nodes.block(new ReturnNode(ageReference))
        );
        TypeResult<Type> result = inferType(functionExpression, standardContext());
        assertThat(result, is(success(CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.NUMBER, CoreTypes.NUMBER))));
    }
    
    @Test public void
    longLambdaExpressionHandlesUnrecognisedArgumentTypes() {
        LongLambdaExpressionNode functionExpression = new LongLambdaExpressionNode(
            asList(
                new FormalArgumentNode("name", new VariableIdentifierNode("Strink"))
            ),
            numberReference,
            Nodes.block(new ReturnNode(new NumberLiteralNode("4")))
        );
        TypeResult<Type> result = inferType(functionExpression, standardContext());
        assertThat(result, isFailureWithErrors(new UntypedReferenceError("Strink")));
    }
    
    @Test public void
    shortLambdaExpressionAddsArgumentsToFunctionScope() {
        FormalArgumentNode ageArgument = new FormalArgumentNode("age", numberReference);
        VariableIdentifierNode ageReference = new VariableIdentifierNode("age");
        references.addReference(ageReference, ageArgument);
        
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            asList(
                new FormalArgumentNode("name", stringReference),
                ageArgument
            ),
            none(ExpressionNode.class),
            ageReference
        );
        TypeResult<Type> result = inferType(functionExpression, standardContext());
        assertThat(result, is(success(CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.NUMBER, CoreTypes.NUMBER))));
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
        TypeResult<Type> result = inferType(functionExpression, standardContext());
        assertThat(result, isFailureWithErrors(new UntypedReferenceError("Strink"), new UntypedReferenceError("Numer")));
    }
    
    @Test public void
    shortLambdaExpressionHandlesUnrecognisedUntypeableBodyWhenReturnTypeIsExplicit() {
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            asList(
                new FormalArgumentNode("age", numberReference)
            ),
            some(numberReference),
            new VariableIdentifierNode("blah")
        );
        TypeResult<Type> result = inferType(functionExpression, standardContext());
        assertThat(result, isFailureWithErrors(new UntypedReferenceError("blah")));
    }
    
    @Test public void
    bodyOfLongLambdaExpressionMustReturn() {
        LongLambdaExpressionNode functionExpression = new LongLambdaExpressionNode(
            Collections.<FormalArgumentNode>emptyList(),
            booleanReference,
            Nodes.block()
        );
        TypeResult<Type> result = inferType(functionExpression, standardContext());
        assertThat(errorStrings(result), is(asList("Expected return statement")));
    }
    
    @Test public void
    functionCallsHaveTypeOfReturnTypeOfFunctionWithNoArguments() {
        VariableIdentifierNode reference = Nodes.id("magic");
        GlobalDeclarationNode declaration = new GlobalDeclarationNode("magic");
        references.addReference(reference, declaration);
        
        StaticContext context = standardContext();
        context.add(declaration, unassignableValue(CoreTypes.functionTypeOf(CoreTypes.NUMBER)));
        
        CallNode call = Nodes.call(reference);
        TypeResult<Type> result = inferType(call, context);
        assertThat(result, isType(CoreTypes.NUMBER));
    }
    
    @Test public void
    functionCallsHaveTypeOfReturnTypeOfFunctionWithCorrectArguments() {
        VariableIdentifierNode reference = Nodes.id("isLength");
        GlobalDeclarationNode declaration = new GlobalDeclarationNode("isLength");
        references.addReference(reference, declaration);
        
        StaticContext context = standardContext();
        // isLength: (String, Number) -> Boolean 
        context.add(declaration, unassignableValue(CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.NUMBER, CoreTypes.BOOLEAN)));
        CallNode call = Nodes.call(reference, Nodes.string("Blah"), Nodes.number("4"));
        TypeResult<Type> result = inferType(call, context);
        assertThat(result, isType(CoreTypes.BOOLEAN));
    }
    
    @Test public void
    errorIfActualArgumentsAreNotAssignableToFormalArguments() {
        VariableIdentifierNode reference = Nodes.id("isLength");
        GlobalDeclarationNode declaration = new GlobalDeclarationNode("isLength");
        references.addReference(reference, declaration);
        
        StaticContext context = standardContext();
        // isLength: (String, Number) -> Boolean 
        context.add(declaration, unassignableValue(CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.NUMBER, CoreTypes.BOOLEAN)));
        CallNode call = Nodes.call(reference, Nodes.number("4"), Nodes.string("Blah"));
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
        VariableIdentifierNode reference = Nodes.id("isLength");
        GlobalDeclarationNode declaration = new GlobalDeclarationNode("isLength");
        references.addReference(reference, declaration);
        
        ClassType classType = new ClassType(fullyQualifiedName("example", "List"));
        ParameterisedType typeFunction = parameterisedType(classType, asList(new FormalTypeParameter("T")));
        StaticContext context = standardContext();
        context.add(declaration, unassignableValue(new TypeApplication(typeFunction, asList((Type)CoreTypes.STRING))));
        
        CallNode call = Nodes.call(reference);
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
        VariableIdentifierNode reference = Nodes.id("isLength");
        GlobalDeclarationNode declaration = new GlobalDeclarationNode("isLength");
        references.addReference(reference, declaration);
        
        StaticContext context = standardContext();
        context.add(declaration, unassignableValue(CoreTypes.BOOLEAN));
        CallNode call = Nodes.call(reference);
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
        VariableIdentifierNode reference = Nodes.id("isLength");
        GlobalDeclarationNode declaration = new GlobalDeclarationNode("isLength");
        references.addReference(reference, declaration);
        
        StaticContext context = standardContext();
        // isLength: (String, Number) -> Boolean 
        context.add(declaration, unassignableValue(CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.NUMBER, CoreTypes.BOOLEAN)));
        
        CallNode call = Nodes.call(reference, Nodes.number("4"));
        TypeResult<Type> result = inferType(call, context);
        assertThat(
            errorStrings(result),
            is(asList("Function requires 2 argument(s), but is called with 1"))
        );
    }
    
    @Test public void
    memberAccessHasTypeOfMember() {
        VariableIdentifierNode reference = Nodes.id("heAintHeavy");
        GlobalDeclarationNode declaration = new GlobalDeclarationNode("heAintHeavy");
        references.addReference(reference, declaration);
        
        StaticContext context = standardContext();
        InterfaceType interfaceType = new InterfaceType(fullyQualifiedName("shed", "example", "Brother"));
        context.add(declaration, unassignableValue(interfaceType));
        context.addInfo(interfaceType, new ScalarTypeInfo(interfaces(), members("age", unassignableValue(CoreTypes.NUMBER))));
        
        MemberAccessNode memberAccess = Nodes.member(reference, "age");
        TypeResult<ValueInfo> result = inferValueInfo(memberAccess, context);
        assertThat(result, is(success(unassignableValue(CoreTypes.NUMBER))));
    }
    
    @Test public void
    memberAccessIsAssignableIfMemberIsAssignable() {
        VariableIdentifierNode reference = Nodes.id("heAintHeavy");
        GlobalDeclarationNode declaration = new GlobalDeclarationNode("heAintHeavy");
        references.addReference(reference, declaration);
        
        StaticContext context = standardContext();
        InterfaceType interfaceType = new InterfaceType(fullyQualifiedName("shed", "example", "Brother"));
        context.add(declaration, unassignableValue(interfaceType));
        context.addInfo(interfaceType, new ScalarTypeInfo(interfaces(), members("age", assignableValue(CoreTypes.NUMBER))));
        
        MemberAccessNode memberAccess = Nodes.member(reference, "age");
        TypeResult<ValueInfo> result = inferValueInfo(memberAccess, context);
        assertThat(result, is(success(assignableValue(CoreTypes.NUMBER))));
    }
    
    @Test public void
    memberAccessFailsIfInterfaceDoesNotHaveSpecifiedMember() {
        VariableIdentifierNode reference = Nodes.id("heAintHeavy");
        GlobalDeclarationNode declaration = new GlobalDeclarationNode("heAintHeavy");
        references.addReference(reference, declaration);
        
        StaticContext context = standardContext();
        InterfaceType interfaceType = new InterfaceType(fullyQualifiedName("shed", "example", "Brother"));
        context.add(declaration, unassignableValue(interfaceType));
        context.addInfo(interfaceType, new ScalarTypeInfo(interfaces(), members("age", unassignableValue(CoreTypes.NUMBER))));
        MemberAccessNode memberAccess = Nodes.member(reference, "height");
        TypeResult<Type> result = inferType(memberAccess, context);
        assertThat(
            errorStrings(result),
            is(asList("No such member: height"))
        );
    }
    
    @Test public void
    applyingTypeUpdatesParameterisedTypeWithType() {
        VariableIdentifierNode listReference = Nodes.id("List");
        GlobalDeclarationNode listDeclaration = new GlobalDeclarationNode("List");
        references.addReference(listReference, listDeclaration);
        
        StaticContext context = standardContext();
        FormalTypeParameter typeParameter = new FormalTypeParameter("T");
        ParameterisedType listTypeFunction = parameterisedType(
            new InterfaceType(fullyQualifiedName("shed", "List")),
            asList(typeParameter)
        );
        context.add(listDeclaration, unassignableValue(listTypeFunction));
        TypeApplicationNode typeApplication = Nodes.typeApply(listReference, numberReference);
        
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            asList(new FormalArgumentNode("dummy", typeApplication)),
            none(ExpressionNode.class),
            new NumberLiteralNode("42")
        );
        TypeResult<Type> result = inferType(functionExpression, context);
        assertThat(result, is((Object)success(
            CoreTypes.functionTypeOf(new TypeApplication(listTypeFunction, asList((Type)CoreTypes.NUMBER)), CoreTypes.NUMBER)
        )));
    }
    
    @Test public void
    applyingTypeUpdatesFunctionArgumentAndReturnTypes() {
        VariableIdentifierNode identityReference = Nodes.id("identity");
        GlobalDeclarationNode identityDeclaration = new GlobalDeclarationNode("identity");
        references.addReference(identityReference, identityDeclaration);
        
        StaticContext context = standardContext();
        
        FormalTypeParameter typeParameter = new FormalTypeParameter("T");
        context.add(identityDeclaration, unassignableValue(new ParameterisedFunctionType(
            CoreTypes.functionTypeOf(typeParameter, typeParameter),
            asList(typeParameter)
        )));
        CallNode call = Nodes.call(Nodes.typeApply(identityReference, numberReference), Nodes.number("2"));
        TypeResult<Type> result = inferType(call, context);
        assertThat(result, isType(CoreTypes.NUMBER));
    }
    
    @Test public void
    assignmentHasTypeOfAssignedValue() {
        VariableIdentifierNode reference = Nodes.id("x");
        GlobalDeclarationNode declaration = new GlobalDeclarationNode("x");
        references.addReference(reference, declaration);
        
        StaticContext context = standardContext();
        
        context.add(declaration, assignableValue(CoreTypes.NUMBER));
        
        TypeResult<Type> result = inferType(Nodes.assign(reference, Nodes.number("4")), context);
        assertThat(result, isType(CoreTypes.NUMBER));
    }
    
    @Test public void
    cannotAssignToUnassignableValue() {
        VariableIdentifierNode reference = Nodes.id("x");
        GlobalDeclarationNode declaration = new GlobalDeclarationNode("x");
        references.addReference(reference, declaration);
        
        StaticContext context = standardContext();
        
        context.add(declaration, unassignableValue(CoreTypes.NUMBER));
        
        TypeResult<Type> result = inferType(Nodes.assign(reference, Nodes.number("4")), context);
        assertThat(result, isFailureWithErrors(new InvalidAssignmentError()));
    }
    
    @Test public void
    cannotAssignValueIfNotSubTypeOfVariableType() {
        VariableIdentifierNode reference = Nodes.id("x");
        GlobalDeclarationNode declaration = new GlobalDeclarationNode("x");
        references.addReference(reference, declaration);
        
        StaticContext context = standardContext();
        
        context.add(declaration, assignableValue(CoreTypes.NUMBER));
        
        TypeResult<Type> result = inferType(Nodes.assign(reference, Nodes.bool(true)), context);
        assertThat(result, isFailureWithErrors(new TypeMismatchError(CoreTypes.NUMBER, CoreTypes.BOOLEAN)));
    }
    
    @Test public void
    canAssignValueIfSubTypeOfVariableType() {
        VariableIdentifierNode interfaceReference = Nodes.id("iterable");
        GlobalDeclarationNode interfaceDeclaration = new GlobalDeclarationNode("iterable");
        references.addReference(interfaceReference, interfaceDeclaration);

        VariableIdentifierNode classReference = Nodes.id("iterable");
        GlobalDeclarationNode classDeclaration = new GlobalDeclarationNode("iterable");
        references.addReference(classReference, classDeclaration);
        
        StaticContext context = standardContext();
        
        InterfaceType interfaceType = new InterfaceType(fullyQualifiedName("shed", "Iterable"));
        ClassType classType = new ClassType(fullyQualifiedName("shed", "List"));
        context.add(interfaceDeclaration, assignableValue(interfaceType));
        context.add(classDeclaration, assignableValue(classType));
        context.addInfo(classType, new ScalarTypeInfo(Interfaces.interfaces(interfaceType), members()));
        
        TypeResult<Type> result = inferType(Nodes.assign(interfaceReference, classReference), context);
        assertThat(result, is(success((Type)classType)));
    }
    
    private TypeResult<Type> inferType(ExpressionNode expression, StaticContext context) {
        return typeInferer().inferType(expression, context);
    }
    
    private TypeResult<ValueInfo> inferValueInfo(ExpressionNode expression, StaticContext context) {
        return typeInferer().inferValueInfo(expression, context);
    }

    private TypeInferer typeInferer() {
        Injector injector = TypeCheckerInjector.build(nodeLocations, new FullyQualifiedNamesBuilder().build());
        return injector.getInstance(TypeInferer.class);
    }
    
    private StaticContext blankContext() {
        return new StaticContext(references.build());
    }
    
    private StaticContext standardContext() {
        references.addReference(numberReference, numberDeclaration);
        references.addReference(stringReference, stringDeclaration);
        references.addReference(booleanReference, booleanDeclaration);
        
        StaticContext context = blankContext();
        context.add(numberDeclaration, unassignableValue(CoreTypes.classOf(CoreTypes.NUMBER)));
        context.add(stringDeclaration, unassignableValue(CoreTypes.classOf(CoreTypes.STRING)));
        context.add(booleanDeclaration, unassignableValue(CoreTypes.classOf(CoreTypes.BOOLEAN)));
        
        return context;
    }
    
    private Matcher<TypeResult<Type>> isType(Type type) {
        return Matchers.is(TypeResult.success(type));
    }
}
