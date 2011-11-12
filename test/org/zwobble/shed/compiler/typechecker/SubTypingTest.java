package org.zwobble.shed.compiler.typechecker;

import org.junit.Test;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.FormalTypeParameter;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.ParameterisedType;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.TypeApplication;
import org.zwobble.shed.compiler.types.TypeMaker;

import com.natpryce.makeiteasy.Instantiator;

import static org.zwobble.shed.compiler.types.FormalTypeParameter.covariantFormalTypeParameter;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.an;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.types.FormalTypeParameter.invariantFormalTypeParameter;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.ParameterisedType.parameterisedType;
import static org.zwobble.shed.compiler.types.TypeApplication.applyTypes;
import static org.zwobble.shed.compiler.types.TypeMaker.superTypes;
import static org.zwobble.shed.compiler.types.Types.typeParameters;

@SuppressWarnings("unchecked")
public class SubTypingTest {
    private final StaticContext context = new StaticContext();
    
    @Test public void
    typeIsSubClassOfItself() {
        ClassType type = make(a(classType()));
        assertThat(isSubType(type, type), is(true));
    }
    
    @Test public void
    unrelatedTypesAreNotSubTypesOfEachOther() {
        ClassType firstType = make(a(classType()));
        ClassType secondType = make(a(classType()));
        assertThat(isSubType(firstType, secondType), is(false));
    }
    
    @Test public void
    classIsSubTypeOfImplementedInterface() {
        InterfaceType interfaceType = make(an(interfaceType()));
        ClassType classType = make(a(classType(), with(superTypes, interfaces(interfaceType))));
        assertThat(isSubType(classType, interfaceType), is(true));
    }
    
    @Test public void
    classIsNotSuperTypeOfImplementedInterface() {
        InterfaceType interfaceType = make(an(interfaceType()));
        ClassType classType = make(a(classType(), with(superTypes, interfaces(interfaceType))));
        assertThat(isSubType(interfaceType, classType), is(false));
    }
    
    @Test public void
    classTypeCanImplementParameterisedInterface() {
        FormalTypeParameter formalTypeParameter = invariantFormalTypeParameter("T");
        InterfaceType interfaceType = make(an(interfaceType()));
        ParameterisedType parameterisedType = parameterisedType(interfaceType, asList(formalTypeParameter));
        TypeApplication concreteInterfaceType = applyTypes(parameterisedType, typeParameters(CoreTypes.BOOLEAN));
        ClassType classType = make(a(classType(), with(superTypes, interfaces(concreteInterfaceType))));
        assertThat(isSubType(classType, applyTypes(parameterisedType, typeParameters(CoreTypes.BOOLEAN))), is(true));
    }
    
    @Test public void
    appliedTypesAreNotSubTypesOfEachOtherIfTheyDifferOnlyInTypeParameters() {
        FormalTypeParameter formalTypeParameter = invariantFormalTypeParameter("T");
        InterfaceType interfaceType = make(an(interfaceType()));
        ParameterisedType parameterisedType = parameterisedType(interfaceType, asList(formalTypeParameter));
        TypeApplication firstType = applyTypes(parameterisedType, typeParameters(CoreTypes.BOOLEAN));
        TypeApplication secondType = applyTypes(parameterisedType, typeParameters(CoreTypes.STRING));
        assertThat(isSubType(firstType, secondType), is(false));
    }
    
    @Test public void
    appliedTypesAreSubTypesOfEachOtherIfTheyAgreeOnParameterisedTypeAndTypeParameters() {
        FormalTypeParameter formalTypeParameter = invariantFormalTypeParameter("T");
        InterfaceType interfaceType = make(an(interfaceType()));
        ParameterisedType parameterisedType = parameterisedType(interfaceType, asList(formalTypeParameter));
        TypeApplication firstType = applyTypes(parameterisedType, typeParameters(CoreTypes.BOOLEAN));
        TypeApplication secondType = applyTypes(parameterisedType, typeParameters(CoreTypes.BOOLEAN));
        assertThat(isSubType(firstType, secondType), is(true));
    }
    
    @Test public void
    firstAppliedTypeIsNotSubTypeOfSecondAppliedTypeIfFormalTypeParameterIsInvariantAndTypeParameterOfFirstIsSubTypeOfTypeParameterOfSecond() {
        InterfaceType interfaceType = make(an(interfaceType()));
        ClassType classType = make(a(classType(), with(superTypes, interfaces(interfaceType))));
        
        FormalTypeParameter formalTypeParameter = invariantFormalTypeParameter("T");
        ParameterisedType parameterisedType = parameterisedType(make(an(interfaceType())), asList(formalTypeParameter));
        TypeApplication firstType = applyTypes(parameterisedType, typeParameters(classType));
        TypeApplication secondType = applyTypes(parameterisedType, typeParameters(interfaceType));
        assertThat(isSubType(firstType, secondType), is(false));
    }
    
    @Test public void
    firstAppliedTypeIsSubTypeOfSecondAppliedTypeIfFormalTypeParameterIsCovariantAndTypeParameterOfFirstIsSubTypeOfTypeParameterOfSecond() {
        InterfaceType interfaceType = make(an(interfaceType()));
        ClassType classType = make(a(classType(), with(superTypes, interfaces(interfaceType))));
        
        FormalTypeParameter formalTypeParameter = covariantFormalTypeParameter("T");
        ParameterisedType parameterisedType = parameterisedType(make(an(interfaceType())), asList(formalTypeParameter));
        TypeApplication firstType = applyTypes(parameterisedType, typeParameters(classType));
        TypeApplication secondType = applyTypes(parameterisedType, typeParameters(interfaceType));
        assertThat(isSubType(firstType, secondType), is(true));
    }

    private boolean isSubType(Type subType, Type superType) {
        return SubTyping.isSubType(subType, superType, context);
    }
    
    private Instantiator<ClassType> classType() {
        return TypeMaker.classType(context);
    }

    private Instantiator<InterfaceType> interfaceType() {
        return TypeMaker.interfaceType(context);
    }
}
