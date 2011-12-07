package org.zwobble.shed.compiler.typechecker;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.zwobble.shed.compiler.CompilerErrorDescription;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.CallNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeApplicationNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.typechecker.errors.InvalidAssignmentError;
import org.zwobble.shed.compiler.typechecker.errors.TypeMismatchError;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.FormalTypeParameter;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.Interfaces;
import org.zwobble.shed.compiler.types.ParameterisedFunctionType;
import org.zwobble.shed.compiler.types.ParameterisedType;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;
import static org.zwobble.shed.compiler.CompilerTesting.isSuccess;
import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration.globalDeclaration;
import static org.zwobble.shed.compiler.typechecker.TypeResultMatchers.isSuccessWithValue;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.assignableValue;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
import static org.zwobble.shed.compiler.types.FormalTypeParameter.invariantFormalTypeParameter;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.Members.members;
import static org.zwobble.shed.compiler.types.ParameterisedType.parameterisedType;
import static org.zwobble.shed.compiler.types.TypeApplication.applyTypes;
import static org.zwobble.shed.compiler.types.Types.typeParameters;

public class TypeInfererTest {
    private final TypeCheckerTestFixture fixture = TypeCheckerTestFixture.build();

    private final VariableIdentifierNode doubleReference = fixture.doubleTypeReference();
    
    @Test public void
    canInferTypeOfBooleanLiteralsAsBoolean() {
        assertThat(inferType(new BooleanLiteralNode(true), standardContext()), isType(CoreTypes.BOOLEAN));
        assertThat(inferType(new BooleanLiteralNode(false), standardContext()), isType(CoreTypes.BOOLEAN));
    }
    
    @Test public void
    canInferTypeOfNumberLiteralsAsNumber() {
        assertThat(inferType(new NumberLiteralNode("2.2"), standardContext()), isType(CoreTypes.DOUBLE));
    }
    
    @Test public void
    canInferTypeOfStringLiteralsAsString() {
        assertThat(inferType(new StringLiteralNode("Everything's as if we never said"), standardContext()), isType(CoreTypes.STRING));
    }
    
    @Test public void
    canInferTypeOfUnitLiteralsAsUnit() {
        assertThat(inferType(Nodes.unit(), standardContext()), isType(CoreTypes.UNIT));
    }
    
    
    
    @Test public void
    applyingTypeUpdatesParameterisedTypeWithType() {
        VariableIdentifierNode listReference = Nodes.id("List");
        GlobalDeclaration listDeclaration = globalDeclaration("List");
        fixture.addReference(listReference, listDeclaration);
        
        StaticContext context = standardContext();
        FormalTypeParameter typeParameter = invariantFormalTypeParameter("T");
        ParameterisedType listTypeFunction = parameterisedType(
            new InterfaceType(fullyQualifiedName("shed", "List")),
            asList(typeParameter)
        );
        context.add(listDeclaration, unassignableValue(listTypeFunction));
        TypeApplicationNode typeApplication = Nodes.typeApply(listReference, doubleReference);
        
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            asList(new FormalArgumentNode("dummy", typeApplication)),
            none(ExpressionNode.class),
            new NumberLiteralNode("42")
        );
        TypeResult<Type> result = inferType(functionExpression, context);
        assertThat(result, isSuccessWithValue(
            (Type)CoreTypes.functionTypeOf(applyTypes(listTypeFunction, asList((Type)CoreTypes.DOUBLE)), CoreTypes.DOUBLE)
        ));
    }
    
    @Test public void
    applyingTypeBuildsMetaClassWithUpdatedMembers() {
        VariableIdentifierNode listReference = Nodes.id("List");
        GlobalDeclaration listDeclaration = globalDeclaration("List");
        fixture.addReference(listReference, listDeclaration);
        
        StaticContext context = standardContext();
        FormalTypeParameter typeParameter = invariantFormalTypeParameter("T");
        ScalarTypeInfo listTypeInfo = new ScalarTypeInfo(interfaces(), members("get", unassignableValue(typeParameter)));
        InterfaceType baseListType = new InterfaceType(fullyQualifiedName("shed", "List"));
        ParameterisedType listTypeFunction = parameterisedType(baseListType, asList(typeParameter));
        context.add(listDeclaration, unassignableValue(listTypeFunction));
        context.addInfo(baseListType, listTypeInfo);
        TypeApplicationNode typeApplication = Nodes.typeApply(listReference, doubleReference);
        
        TypeResult<Type> result = inferType(typeApplication, context);
        assertThat(result, isSuccess());
        Type metaClass = result.getOrThrow();
        ScalarType type = (ScalarType) fixture.metaClasses().getTypeFromMetaClass((ClassType)metaClass);
        ScalarTypeInfo typeInfo = context.getInfo(type);
        assertThat(typeInfo.getMembers(), is(members("get", unassignableValue(CoreTypes.DOUBLE))));
    }
    
    @Test public void
    applyingTypeUpdatesFunctionArgumentAndReturnTypes() {
        VariableIdentifierNode identityReference = Nodes.id("identity");
        GlobalDeclaration identityDeclaration = globalDeclaration("identity");
        fixture.addReference(identityReference, identityDeclaration);
        
        StaticContext context = standardContext();
        
        FormalTypeParameter typeParameter = invariantFormalTypeParameter("T");
        context.add(identityDeclaration, unassignableValue(new ParameterisedFunctionType(
            typeParameters(typeParameter, typeParameter),
            asList(typeParameter)
        )));
        CallNode call = Nodes.call(Nodes.typeApply(identityReference, doubleReference), Nodes.number("2"));
        TypeResult<Type> result = inferType(call, context);
        assertThat(result, isType(CoreTypes.DOUBLE));
    }
    
    @Test public void
    assignmentHasTypeOfAssignedValue() {
        VariableIdentifierNode reference = Nodes.id("x");
        GlobalDeclaration declaration = globalDeclaration("x");
        fixture.addReference(reference, declaration);
        
        StaticContext context = standardContext();
        
        context.add(declaration, assignableValue(CoreTypes.DOUBLE));
        
        TypeResult<Type> result = inferType(Nodes.assign(reference, Nodes.number("4")), context);
        assertThat(result, isType(CoreTypes.DOUBLE));
    }
    
    @Test public void
    cannotAssignToUnassignableValue() {
        VariableIdentifierNode reference = Nodes.id("x");
        GlobalDeclaration declaration = globalDeclaration("x");
        fixture.addReference(reference, declaration);
        
        StaticContext context = standardContext();
        
        context.add(declaration, unassignableValue(CoreTypes.DOUBLE));
        
        TypeResult<Type> result = inferType(Nodes.assign(reference, Nodes.number("4")), context);
        CompilerErrorDescription[] errorsArray = { new InvalidAssignmentError() };
        assertThat(result, isFailureWithErrors(errorsArray));
    }
    
    @Test public void
    cannotAssignValueIfNotSubTypeOfVariableType() {
        VariableIdentifierNode reference = Nodes.id("x");
        GlobalDeclaration declaration = globalDeclaration("x");
        fixture.addReference(reference, declaration);
        
        StaticContext context = standardContext();
        
        context.add(declaration, assignableValue(CoreTypes.DOUBLE));
        
        TypeResult<Type> result = inferType(Nodes.assign(reference, Nodes.bool(true)), context);
        CompilerErrorDescription[] errorsArray = { new TypeMismatchError(CoreTypes.DOUBLE, CoreTypes.BOOLEAN) };
        assertThat(result, isFailureWithErrors(errorsArray));
    }
    
    @Test public void
    canAssignValueIfSubTypeOfVariableType() {
        VariableIdentifierNode interfaceReference = Nodes.id("iterable");
        GlobalDeclaration interfaceDeclaration = globalDeclaration("iterable");
        fixture.addReference(interfaceReference, interfaceDeclaration);

        VariableIdentifierNode classReference = Nodes.id("iterable");
        GlobalDeclaration classDeclaration = globalDeclaration("iterable");
        fixture.addReference(classReference, classDeclaration);
        
        StaticContext context = standardContext();
        
        InterfaceType interfaceType = new InterfaceType(fullyQualifiedName("shed", "Iterable"));
        ClassType classType = new ClassType(fullyQualifiedName("shed", "List"));
        context.add(interfaceDeclaration, assignableValue(interfaceType));
        context.add(classDeclaration, assignableValue(classType));
        context.addInfo(classType, new ScalarTypeInfo(Interfaces.interfaces(interfaceType), members()));
        
        TypeResult<Type> result = inferType(Nodes.assign(interfaceReference, classReference), context);
        assertThat(result, isSuccessWithValue((Type)classType));
    }
    
    private TypeResult<Type> inferType(ExpressionNode expression, StaticContext context) {
        return typeInferer(context).inferType(expression);
    }

    private TypeInferer typeInferer(StaticContext context) {
        return fixture.get(TypeInferer.class);
    }
    
    private StaticContext standardContext() {
        return fixture.context();
    }
    
    private Matcher<TypeResult<Type>> isType(Type type) {
        return isSuccessWithValue(type);
    }
}
