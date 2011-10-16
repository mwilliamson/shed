package org.zwobble.shed.compiler.types;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;

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
    
    @Test public void
    membersOfClassTypeHaveFormalTypeParametersReplaced() {
        TypeReplacer typeReplacer = new TypeReplacer();
        FormalTypeParameter formalTypeParameter = new FormalTypeParameter("T");

        Type replacement = typeReplacer.replaceTypes(
            new ClassType(
                fullyQualifiedName("shed", "example", "List"),
                Collections.<InterfaceType>emptySet(),
                ImmutableMap.of("first", unassignableValue(formalTypeParameter))
            ),
            ImmutableMap.<FormalTypeParameter, Type>of(formalTypeParameter, CoreTypes.NUMBER)
        );
        assertThat(replacement, is((Type)new ClassType(
            fullyQualifiedName("shed", "example", "List"),
            Collections.<InterfaceType>emptySet(),
            ImmutableMap.of("first", unassignableValue(CoreTypes.NUMBER))
        )));
    }
    
    @Test public void
    membersOfInterfaceTypeHaveFormalTypeParametersReplaced() {
        TypeReplacer typeReplacer = new TypeReplacer();
        FormalTypeParameter formalTypeParameter = new FormalTypeParameter("T");

        Type replacement = typeReplacer.replaceTypes(
            new InterfaceType(
                fullyQualifiedName("shed", "example", "List"),
                ImmutableMap.of("first", unassignableValue(formalTypeParameter))
            ),
            ImmutableMap.<FormalTypeParameter, Type>of(formalTypeParameter, CoreTypes.NUMBER)
        );
        assertThat(replacement, is((Type)new InterfaceType(
            fullyQualifiedName("shed", "example", "List"),
            ImmutableMap.of("first", unassignableValue(CoreTypes.NUMBER))
        )));
    }
    
    @Test public void
    membersAndParametersOfTypeApplicationHaveFormalTypeParametersReplaced() {
        TypeReplacer typeReplacer = new TypeReplacer();
        FormalTypeParameter formalTypeParameter = new FormalTypeParameter("T");

        InterfaceType listInterface = new InterfaceType(
            fullyQualifiedName("shed", "example", "List"),
            ImmutableMap.of("first", unassignableValue(new FormalTypeParameter("E")))
        );
        Type replacement = typeReplacer.replaceTypes(
            new TypeApplication(
                new InterfaceType(
                    fullyQualifiedName("shed", "example", "List"),
                    ImmutableMap.of("first", unassignableValue(formalTypeParameter))
                ),
                listInterface,
                Arrays.<Type>asList(formalTypeParameter)
            ),
            ImmutableMap.<FormalTypeParameter, Type>of(formalTypeParameter, CoreTypes.NUMBER)
        );
        assertThat(replacement, is((Type)new TypeApplication(
            new InterfaceType(
                fullyQualifiedName("shed", "example", "List"),
                ImmutableMap.of("first", unassignableValue(CoreTypes.NUMBER))
            ),
            listInterface,
            Arrays.<Type>asList(CoreTypes.NUMBER)
        )));
    }
}
