package org.zwobble.shed.compiler.typechecker.expressions;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.CallNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeApplicationNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeCheckerTestFixture;
import org.zwobble.shed.compiler.typechecker.TypeInferer;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.FormalTypeParameter;
import org.zwobble.shed.compiler.types.ScalarFormalTypeParameter;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.ParameterisedFunctionType;
import org.zwobble.shed.compiler.types.ParameterisedType;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.isSuccess;
import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration.globalDeclaration;
import static org.zwobble.shed.compiler.typechecker.TypeResultMatchers.isSuccessWithValue;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
import static org.zwobble.shed.compiler.types.ScalarFormalTypeParameter.invariantFormalTypeParameter;
import static org.zwobble.shed.compiler.types.FormalTypeParameters.formalTypeParameters;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.Members.members;
import static org.zwobble.shed.compiler.types.ParameterisedType.parameterisedType;
import static org.zwobble.shed.compiler.types.TypeApplication.applyTypes;
import static org.zwobble.shed.compiler.types.Types.typeParameters;

public class TypeApplicationTypeInfererTest {
    private final TypeCheckerTestFixture fixture = TypeCheckerTestFixture.build();

    private final VariableIdentifierNode doubleReference = fixture.doubleTypeReference();    
    
    @Test public void
    applyingTypeUpdatesParameterisedTypeWithType() {
        VariableIdentifierNode listReference = Nodes.id("List");
        GlobalDeclaration listDeclaration = globalDeclaration("List");
        fixture.addReference(listReference, listDeclaration);
        
        StaticContext context = fixture.context();
        FormalTypeParameter typeParameter = invariantFormalTypeParameter("T");
        ParameterisedType listTypeFunction = parameterisedType(
            new InterfaceType(fullyQualifiedName("shed", "List")),
            formalTypeParameters(typeParameter)
        );
        context.add(listDeclaration, unassignableValue(listTypeFunction));
        TypeApplicationNode typeApplication = Nodes.typeApply(listReference, doubleReference);
        
        ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
            asList(new FormalArgumentNode("dummy", typeApplication)),
            none(ExpressionNode.class),
            new NumberLiteralNode("42")
        );
        TypeResult<Type> result = inferType(functionExpression);
        assertThat(result, isSuccessWithValue(
            (Type)CoreTypes.functionTypeOf(applyTypes(listTypeFunction, asList((Type)CoreTypes.DOUBLE)), CoreTypes.DOUBLE)
        ));
    }
    
    @Test public void
    applyingTypeBuildsMetaClassWithUpdatedMembers() {
        VariableIdentifierNode listReference = Nodes.id("List");
        GlobalDeclaration listDeclaration = globalDeclaration("List");
        fixture.addReference(listReference, listDeclaration);
        
        StaticContext context = fixture.context();
        FormalTypeParameter typeParameter = ScalarFormalTypeParameter.invariantFormalTypeParameter("T");
        ScalarTypeInfo listTypeInfo = new ScalarTypeInfo(interfaces(), members("get", unassignableValue(typeParameter)));
        InterfaceType baseListType = new InterfaceType(fullyQualifiedName("shed", "List"));
        ParameterisedType listTypeFunction = parameterisedType(baseListType, formalTypeParameters(typeParameter));
        context.add(listDeclaration, unassignableValue(listTypeFunction));
        context.addInfo(baseListType, listTypeInfo);
        TypeApplicationNode typeApplication = Nodes.typeApply(listReference, doubleReference);
        
        TypeResult<Type> result = inferType(typeApplication);
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
        
        StaticContext context = fixture.context();
        
        FormalTypeParameter typeParameter = invariantFormalTypeParameter("T");
        context.add(identityDeclaration, unassignableValue(new ParameterisedFunctionType(
            typeParameters(typeParameter, typeParameter),
            formalTypeParameters(typeParameter)
        )));
        CallNode call = Nodes.call(Nodes.typeApply(identityReference, doubleReference), Nodes.number("2"));
        TypeResult<Type> result = inferType(call);
        assertThat(result, isType(CoreTypes.DOUBLE));
    }
    
    private TypeResult<Type> inferType(ExpressionNode expression) {
        return typeInferer().inferType(expression);
    }

    private TypeInferer typeInferer() {
        return fixture.get(TypeInferer.class);
    }
    
    private Matcher<TypeResult<Type>> isType(Type type) {
        return isSuccessWithValue(type);
    }
}
