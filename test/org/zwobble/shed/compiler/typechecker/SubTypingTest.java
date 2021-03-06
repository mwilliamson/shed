package org.zwobble.shed.compiler.typechecker;

import org.junit.Test;
import org.zwobble.shed.compiler.metaclassgeneration.MetaClasses;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.FormalTypeParameter;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.ParameterisedType;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.TypeApplication;
import org.zwobble.shed.compiler.types.TypeMaker;

import com.natpryce.makeiteasy.Instantiator;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.an;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.types.FormalTypeParameters.formalTypeParameters;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.ParameterisedType.parameterisedType;
import static org.zwobble.shed.compiler.types.ScalarFormalTypeParameter.contravariantFormalTypeParameter;
import static org.zwobble.shed.compiler.types.ScalarFormalTypeParameter.covariantFormalTypeParameter;
import static org.zwobble.shed.compiler.types.ScalarFormalTypeParameter.invariantFormalTypeParameter;
import static org.zwobble.shed.compiler.types.TypeApplication.applyTypes;
import static org.zwobble.shed.compiler.types.TypeMaker.interfaces;
import static org.zwobble.shed.compiler.types.Types.typeParameters;

@SuppressWarnings("unchecked")
public class SubTypingTest {
    private final MetaClasses metaClasses = MetaClasses.create();
    private final StaticContext context = new StaticContext(metaClasses);
    private final FormalTypeParameter invariantTypeParameter = invariantFormalTypeParameter("TInvariant");
    private final FormalTypeParameter covariantTypeParameter = covariantFormalTypeParameter("TCovariant");
    private final FormalTypeParameter contravariantTypeParameter = contravariantFormalTypeParameter("TContravariant");

    private final InterfaceType interfaceType = make(an(interfaceType()));
    private final ClassType implementingClassType = make(a(classType(), with(interfaces, interfaces(interfaceType))));    
    
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
        ClassType classType = make(a(classType(), with(interfaces, interfaces(interfaceType))));
        assertThat(isSubType(classType, interfaceType), is(true));
    }
    
    @Test public void
    classIsNotSuperTypeOfImplementedInterface() {
        InterfaceType interfaceType = make(an(interfaceType()));
        ClassType classType = make(a(classType(), with(interfaces, interfaces(interfaceType))));
        assertThat(isSubType(interfaceType, classType), is(false));
    }
    
    @Test public void
    classTypeCanImplementParameterisedInterface() {
        ParameterisedType parameterisedType = parameterisedType(interfaceType, formalTypeParameters(invariantTypeParameter));
        TypeApplication concreteInterfaceType = applyTypes(parameterisedType, typeParameters(CoreTypes.BOOLEAN));
        ClassType classType = make(a(classType(), with(interfaces, interfaces(concreteInterfaceType))));
        assertThat(isSubType(classType, applyTypes(parameterisedType, typeParameters(CoreTypes.BOOLEAN))), is(true));
    }
    
    @Test public void
    appliedTypesAreNotSubTypesOfEachOtherIfTheyDifferOnlyInTypeParameters() {
        ParameterisedType parameterisedType = parameterisedType(interfaceType, formalTypeParameters(invariantTypeParameter));
        TypeApplication firstType = applyTypes(parameterisedType, typeParameters(CoreTypes.BOOLEAN));
        TypeApplication secondType = applyTypes(parameterisedType, typeParameters(CoreTypes.STRING));
        assertThat(isSubType(firstType, secondType), is(false));
    }
    
    @Test public void
    appliedTypesAreSubTypesOfEachOtherIfTheyAgreeOnParameterisedTypeAndTypeParameters() {
        ParameterisedType parameterisedType = parameterisedType(interfaceType, formalTypeParameters(invariantTypeParameter));
        TypeApplication firstType = applyTypes(parameterisedType, typeParameters(CoreTypes.BOOLEAN));
        TypeApplication secondType = applyTypes(parameterisedType, typeParameters(CoreTypes.BOOLEAN));
        assertThat(isSubType(firstType, secondType), is(true));
    }
    
    @Test public void
    appliedTypesAreNotSubTypesIfTypeFunctionsDiffer() {
        ParameterisedType firstParameterisedType = parameterisedType(interfaceType, formalTypeParameters(invariantTypeParameter));
        ParameterisedType secondParameterisedType = parameterisedType(make(an(interfaceType())), formalTypeParameters(invariantTypeParameter));
        TypeApplication firstType = applyTypes(firstParameterisedType, typeParameters(CoreTypes.BOOLEAN));
        TypeApplication secondType = applyTypes(secondParameterisedType, typeParameters(CoreTypes.BOOLEAN));
        assertThat(isSubType(firstType, secondType), is(false));
    }
    
    @Test public void
    firstAppliedTypeIsNotSubTypeOfSecondAppliedTypeIfFormalTypeParameterIsInvariantAndTypeParameterOfFirstIsSubTypeOfTypeParameterOfSecond() {
        ParameterisedType parameterisedType = parameterisedType(make(an(interfaceType())), formalTypeParameters(invariantTypeParameter));
        TypeApplication firstType = applyTypes(parameterisedType, typeParameters(implementingClassType));
        TypeApplication secondType = applyTypes(parameterisedType, typeParameters(interfaceType));
        assertThat(isSubType(firstType, secondType), is(false));
    }
    
    @Test public void
    firstAppliedTypeIsSubTypeOfSecondAppliedTypeIfFormalTypeParameterIsCovariantAndTypeParameterOfFirstIsSubTypeOfTypeParameterOfSecond() {
        ParameterisedType parameterisedType = parameterisedType(make(an(interfaceType())), formalTypeParameters(covariantTypeParameter));
        TypeApplication firstType = applyTypes(parameterisedType, typeParameters(implementingClassType));
        TypeApplication secondType = applyTypes(parameterisedType, typeParameters(interfaceType));
        assertThat(isSubType(firstType, secondType), is(true));
    }
    
    @Test public void
    firstAppliedTypeIsNotSubTypeOfSecondAppliedTypeIfFormalTypeParameterIsCovariantAndTypeParameterOfFirstIsSuperTypeOfTypeParameterOfSecond() {
        ParameterisedType parameterisedType = parameterisedType(make(an(interfaceType())), formalTypeParameters(covariantTypeParameter));
        TypeApplication firstType = applyTypes(parameterisedType, typeParameters(interfaceType));
        TypeApplication secondType = applyTypes(parameterisedType, typeParameters(implementingClassType));
        assertThat(isSubType(firstType, secondType), is(false));
    }
    
    @Test public void
    firstAppliedTypeIsSuperTypeOfSecondAppliedTypeIfFormalTypeParameterIsContravariantAndTypeParameterOfFirstIsSuperTypeOfTypeParameterOfSecond() {
        ParameterisedType parameterisedType = parameterisedType(make(an(interfaceType())), formalTypeParameters(contravariantTypeParameter));
        TypeApplication firstType = applyTypes(parameterisedType, typeParameters(interfaceType));
        TypeApplication secondType = applyTypes(parameterisedType, typeParameters(implementingClassType));
        assertThat(isSubType(firstType, secondType), is(true));
    }
    
    @Test public void
    firstAppliedTypeIsNotSubTypeOfSecondAppliedTypeIfFormalTypeParameterIsContravariantAndTypeParameterOfFirstIsSubTypeOfTypeParameterOfSecond() {
        ParameterisedType parameterisedType = parameterisedType(make(an(interfaceType())), formalTypeParameters(contravariantTypeParameter));
        TypeApplication firstType = applyTypes(parameterisedType, typeParameters(implementingClassType));
        TypeApplication secondType = applyTypes(parameterisedType, typeParameters(interfaceType));
        assertThat(isSubType(firstType, secondType), is(false));
    }
    
    @Test public void
    allTypesAreSubTypeOfAnyType() {
        assertThat(isSubType(CoreTypes.BOOLEAN, CoreTypes.ANY), is(true));
    }
    
    @Test public void
    anyIsNotSubTypeOfOtherTypes() {
        assertThat(isSubType(CoreTypes.ANY, CoreTypes.BOOLEAN), is(false));
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
