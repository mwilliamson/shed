package org.zwobble.shed.compiler.types;

import org.junit.Test;
import org.zwobble.shed.compiler.typechecker.DefaultContext;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.SubTyping;

import com.natpryce.makeiteasy.Instantiator;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.an;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.types.CoreTypes.functionTypeOf;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.TypeMaker.interfaces;

@SuppressWarnings("unchecked")
public class CoreTypesTest {
    private final StaticContext context = DefaultContext.defaultContext();
    private final InterfaceType interfaceType = make(an(interfaceType()));
    private final ClassType implementingClassType = make(a(classType(), with(interfaces, interfaces(interfaceType))));    
    
    @Test public void
    functionTypeIsCovariantInReturnType() {
        ScalarType superType = functionTypeOf(interfaceType);
        ScalarType subType = functionTypeOf(implementingClassType);
        assertThat(isSubType(subType, superType), is(true));
        assertThat(isSubType(superType, subType), is(false));
    }
    @Test public void
    functionTypeIsContravariantInArgumentTypes() {
        ScalarType superType = functionTypeOf(implementingClassType, CoreTypes.BOOLEAN);
        ScalarType subType = functionTypeOf(interfaceType, CoreTypes.BOOLEAN);
        assertThat(isSubType(subType, superType), is(true));
        assertThat(isSubType(superType, subType), is(false));
    }

    private boolean isSubType(Type subType, Type superType) {
        return new SubTyping(context).isSubType(subType, superType);
    }
    
    private Instantiator<ClassType> classType() {
        return TypeMaker.classType(context);
    }

    private Instantiator<InterfaceType> interfaceType() {
        return TypeMaker.interfaceType(context);
    }
}
