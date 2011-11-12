package org.zwobble.shed.compiler.types;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import static org.zwobble.shed.compiler.types.FormalTypeParameter.invariantFormalTypeParameter;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.types.ParameterisedType.parameterisedType;
import static org.zwobble.shed.compiler.types.TypeApplication.applyTypes;

public class TypeReplacerTest {
    @Test public void
    formalTypeParameterIsReplacedWithItselfIfNotInReplacementMap() {
        TypeReplacer typeReplacer = new TypeReplacer();
        FormalTypeParameter formalTypeParameter = invariantFormalTypeParameter("T");
        Type replacement = typeReplacer.replaceTypes(formalTypeParameter, ImmutableMap.<FormalTypeParameter, Type>of());
        assertThat(replacement, sameInstance((Type)formalTypeParameter));
    }
    
    @Test public void
    formalTypeParameterIsNotReplacedWhenReplacingDifferentTypeParameterWithSameName() {
        TypeReplacer typeReplacer = new TypeReplacer();
        FormalTypeParameter formalTypeParameter = invariantFormalTypeParameter("T");
        Type replacement = typeReplacer.replaceTypes(
            formalTypeParameter,
            ImmutableMap.of(invariantFormalTypeParameter("T"), (Type)CoreTypes.DOUBLE)
        );
        assertThat(replacement, sameInstance((Type)formalTypeParameter));
    }
    
    @Test public void
    formalTypeParameterIsReplacedIfPresentInReplacements() {
        TypeReplacer typeReplacer = new TypeReplacer();
        FormalTypeParameter formalTypeParameter = invariantFormalTypeParameter("T");
        Type replacement = typeReplacer.replaceTypes(
            formalTypeParameter,
            ImmutableMap.of(formalTypeParameter, (Type)CoreTypes.DOUBLE)
        );
        assertThat(replacement, is((Type)CoreTypes.DOUBLE));
    }
    
    @Test public void
    typeParametersOfApplicationAreReplaced() {
        TypeReplacer typeReplacer = new TypeReplacer();
        
        FormalTypeParameter listFormalTypeParameter = invariantFormalTypeParameter("E");
        FormalTypeParameter functionFormalTypeParameter = invariantFormalTypeParameter("T");
        ClassType scalarType = new ClassType(fullyQualifiedName("shed", "example", "List"));
        ParameterisedType parameterisedType = parameterisedType(scalarType, asList(listFormalTypeParameter));
        Type typeApplication = applyTypes(parameterisedType, asList((Type)functionFormalTypeParameter));
        
        Type replacement = typeReplacer.replaceTypes(
            typeApplication,
            ImmutableMap.<FormalTypeParameter, Type>of(functionFormalTypeParameter, CoreTypes.DOUBLE)
        );
        assertThat(replacement, is((Type)applyTypes(parameterisedType, asList((Type)CoreTypes.DOUBLE))));
    }
}
