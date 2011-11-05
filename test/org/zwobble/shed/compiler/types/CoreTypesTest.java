package org.zwobble.shed.compiler.types;

import org.junit.Test;
import org.zwobble.shed.compiler.typechecker.StaticContext;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.types.CoreTypes.isFunction;
import static org.zwobble.shed.compiler.types.FormalTypeParameter.formalTypeParameter;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.Members.members;
import static org.zwobble.shed.compiler.types.ParameterisedType.parameterisedType;
import static org.zwobble.shed.compiler.types.TypeApplication.applyTypes;
import static org.zwobble.shed.compiler.types.Types.typeParameters;

public class CoreTypesTest {
    @Test public void
    typeApplicationsWithFunctionAsBaseTypeAreConsideredFunctions() {
        assertThat(isFunction(CoreTypes.functionTypeOf(CoreTypes.STRING), new StaticContext()), is(true));
    }
    
    @Test public void
    typeApplicationsWithoutFunctionAsBaseTypeIsNotFunction() {
        StaticContext context = new StaticContext();
        ClassType classType = new ClassType(fullyQualifiedName());
        context.addInfo(classType, ScalarTypeInfo.EMPTY);
        ParameterisedType parameterisedType = parameterisedType(classType, asList(formalTypeParameter("T")));
        assertThat(isFunction(applyTypes(parameterisedType, typeParameters(CoreTypes.STRING)), context), is(false));
    }
    
    @Test public void
    typeIsFunctionIfItDirectlyImplementsFunctionInterface() {
        ClassType classType = new ClassType(fullyQualifiedName());
        ScalarTypeInfo typeInfo = new ScalarTypeInfo(interfaces(CoreTypes.functionTypeOf(CoreTypes.STRING)), members());
        StaticContext context = new StaticContext();
        context.addInfo(classType, typeInfo);
        assertThat(isFunction(classType, context), is(true));
    }
    
    @Test public void
    typeIsFunctionIfASuperTypeDirectlyImplementsFunctionInterface() {
        InterfaceType interfaceType = new InterfaceType(fullyQualifiedName());
        ScalarTypeInfo interfaceTypeInfo = new ScalarTypeInfo(interfaces(CoreTypes.functionTypeOf(CoreTypes.STRING)), members());
        ClassType classType = new ClassType(fullyQualifiedName());
        ScalarTypeInfo classTypeInfo = new ScalarTypeInfo(interfaces(interfaceType), members());
        StaticContext context = new StaticContext();
        context.addInfo(classType, classTypeInfo);
        context.addInfo(interfaceType, interfaceTypeInfo);
        assertThat(isFunction(classType, context), is(true));
    }
}
