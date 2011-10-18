package org.zwobble.shed.compiler.types;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.types.ParameterisedType.parameterisedType;

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
            ImmutableMap.of(new FormalTypeParameter("T"), (Type)CoreTypes.NUMBER)
        );
        assertThat(replacement, sameInstance((Type)formalTypeParameter));
    }
    
    @Test public void
    formalTypeParameterIsReplacedIfPresentInReplacements() {
        TypeReplacer typeReplacer = new TypeReplacer();
        FormalTypeParameter formalTypeParameter = new FormalTypeParameter("T");
        Type replacement = typeReplacer.replaceTypes(
            formalTypeParameter,
            ImmutableMap.of(formalTypeParameter, (Type)CoreTypes.NUMBER)
        );
        assertThat(replacement, is((Type)CoreTypes.NUMBER));
    }
    
    @Test public void
    typeParametersOfApplicationAreReplaced() {
        TypeReplacer typeReplacer = new TypeReplacer();
        
        FormalTypeParameter listFormalTypeParameter = new FormalTypeParameter("E");
        FormalTypeParameter functionFormalTypeParameter = new FormalTypeParameter("T");
        ClassType scalarType = new ClassType(fullyQualifiedName("shed", "example", "List"));
        ParameterisedType parameterisedType = parameterisedType(scalarType, asList(listFormalTypeParameter));
        Type typeApplication = new TypeApplication(parameterisedType, asList((Type)functionFormalTypeParameter));
        
        Type replacement = typeReplacer.replaceTypes(
            typeApplication,
            ImmutableMap.<FormalTypeParameter, Type>of(functionFormalTypeParameter, CoreTypes.NUMBER)
        );
        assertThat(replacement, is((Type)new TypeApplication(parameterisedType, asList((Type)CoreTypes.NUMBER))));
    }
}
