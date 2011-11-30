package org.zwobble.shed.compiler.typechecker;

import org.junit.Test;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.ParameterisedType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.types.FormalTypeParameter.invariantFormalTypeParameter;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.Members.members;
import static org.zwobble.shed.compiler.types.ParameterisedType.parameterisedType;
import static org.zwobble.shed.compiler.types.TypeApplication.applyTypes;
import static org.zwobble.shed.compiler.types.Types.typeParameters;

public class FunctionTypingTest {
    private final StaticContext context = new StaticContext();
    
    @Test public void
    typeApplicationsWithFunctionAsBaseTypeAreConsideredFunctions() {
        assertThat(isFunction(CoreTypes.functionTypeOf(CoreTypes.STRING), context), is(true));
    }

    @Test public void
    typeApplicationsWithoutFunctionAsBaseTypeIsNotFunction() {
        ClassType classType = new ClassType(fullyQualifiedName());
        context.addInfo(classType, ScalarTypeInfo.EMPTY);
        ParameterisedType parameterisedType = parameterisedType(classType, asList(invariantFormalTypeParameter("T")));
        assertThat(isFunction(applyTypes(parameterisedType, typeParameters(CoreTypes.STRING)), context), is(false));
    }
    
    @Test public void
    typeIsFunctionIfItDirectlyImplementsFunctionInterface() {
        ClassType classType = new ClassType(fullyQualifiedName());
        ScalarTypeInfo typeInfo = new ScalarTypeInfo(interfaces(CoreTypes.functionTypeOf(CoreTypes.STRING)), members());
        context.addInfo(classType, typeInfo);
        assertThat(isFunction(classType, context), is(true));
    }
    
    @Test public void
    typeIsFunctionIfASuperTypeDirectlyImplementsFunctionInterface() {
        InterfaceType interfaceType = new InterfaceType(fullyQualifiedName());
        ScalarTypeInfo interfaceTypeInfo = new ScalarTypeInfo(interfaces(CoreTypes.functionTypeOf(CoreTypes.STRING)), members());
        ClassType classType = new ClassType(fullyQualifiedName());
        ScalarTypeInfo classTypeInfo = new ScalarTypeInfo(interfaces(interfaceType), members());
        context.addInfo(classType, classTypeInfo);
        context.addInfo(interfaceType, interfaceTypeInfo);
        assertThat(isFunction(classType, context), is(true));
    }
    
    @Test public void
    typeFunctionsAreNotFunctions() {
        InterfaceType interfaceType = new InterfaceType(fullyQualifiedName());
        assertThat(isFunction(ParameterisedType.parameterisedType(interfaceType, asList(invariantFormalTypeParameter("T"))), context), is(false));
    }
    
    private boolean isFunction(Type type, StaticContext staticContext) {
        return new FunctionTyping(staticContext).isFunction(type);
    }
}
