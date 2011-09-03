package org.zwobble.shed.compiler.types;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

public class TypeReplacerTest {
    @Test public void
    formalTypeParameterIsReplacedWithItselfIfNotInReplacementMap() {
        TypeReplacer typeReplacer = new TypeReplacer();
        FormalTypeParameter formalTypeParameter = new FormalTypeParameter("T");
        Type replacement = typeReplacer.replaceTypes(formalTypeParameter, ImmutableMap.<FormalTypeParameter, Type>of());
        assertThat(replacement, sameInstance((Type)formalTypeParameter));
    }
    
    @Test public void
    formalTypeParameterIsNotReplacedWhenReplacingDifferentTypeParameterWithSameName() {
        TypeReplacer typeReplacer = new TypeReplacer();
        FormalTypeParameter formalTypeParameter = new FormalTypeParameter("T");
        Type replacement = typeReplacer.replaceTypes(
            formalTypeParameter,
            ImmutableMap.of(new FormalTypeParameter("T"), CoreTypes.NUMBER)
        );
        assertThat(replacement, sameInstance((Type)formalTypeParameter));
    }
    
    @Test public void
    formalTypeParameterIsReplacedIfPresentInReplacements() {
        TypeReplacer typeReplacer = new TypeReplacer();
        FormalTypeParameter formalTypeParameter = new FormalTypeParameter("T");
        Type replacement = typeReplacer.replaceTypes(
            formalTypeParameter,
            ImmutableMap.of(formalTypeParameter, CoreTypes.NUMBER)
        );
        assertThat(replacement, is(CoreTypes.NUMBER));
    }
    
    @Test public void
    typeApplicationAppliedToFunctionTypeIsReplacedByFunctionTypesBaseTypeWithAppropriateFormalTypeParametersReplaced() {
        TypeReplacer typeReplacer = new TypeReplacer();
        FormalTypeParameter formalTypeParameter = new FormalTypeParameter("T");
        
        Type typeApplication = TypeApplication.applyTypes(
            new ParameterisedFunctionType(
                CoreTypes.functionTypeOf(formalTypeParameter, formalTypeParameter),
                asList(formalTypeParameter)
            ),
            asList(CoreTypes.NUMBER)
        );
        
        Type replacement = typeReplacer.replaceTypes(
            typeApplication,
            ImmutableMap.<FormalTypeParameter, Type>of()
        );
        assertThat(replacement, is((Type)CoreTypes.functionTypeOf(CoreTypes.NUMBER, CoreTypes.NUMBER)));
    }
}
