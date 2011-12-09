package org.zwobble.shed.compiler.types;

import java.util.Map;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.shed.compiler.types.FormalTypeParameters.formalTypeParameters;
import static org.zwobble.shed.compiler.types.ScalarFormalTypeParameter.invariantFormalTypeParameter;

public class FormalTypeParametersTest {
    private final FormalTypeParameter variadic = VariadicFormalTypeParameter.invariant("V");
    private final FormalTypeParameter invariantScalar1 = invariantFormalTypeParameter("T1");
    private final FormalTypeParameter invariantScalar2 = invariantFormalTypeParameter("T2");
    private final FormalTypeParameter invariantScalar3 = invariantFormalTypeParameter("T3");
    
    @Test public void
    scalarFormalTypeParametersAreMappedOneToOneWithActualTypeParameters() {
        FormalTypeParameters formalTypeParameters = formalTypeParameters(invariantScalar1, invariantScalar2);
        Iterable<Type> actualTypeParameters = asList((Type)CoreTypes.BOOLEAN, CoreTypes.STRING);
        Map<FormalTypeParameter, Type> replacementMap = formalTypeParameters.replacementMap(actualTypeParameters);
        assertThat(replacementMap, isMap(invariantScalar1, CoreTypes.BOOLEAN, invariantScalar2, CoreTypes.STRING));
    }
    
    @Test public void
    variadicConsumeAllTypeParametersIfThereAreNoScalarTypeParameters() {
        FormalTypeParameters formalTypeParameters = formalTypeParameters(variadic);
        Iterable<Type> actualTypeParameters = asList((Type)CoreTypes.BOOLEAN, CoreTypes.STRING);
        Map<FormalTypeParameter, Type> replacementMap = formalTypeParameters.replacementMap(actualTypeParameters);
        assertThat(replacementMap, isMap(variadic, CoreTypes.tupleOf(CoreTypes.BOOLEAN, CoreTypes.STRING)));
    }
    
    @Test public void
    variadicConsumeAllTypeParametersThatArentMatchedWithStartingOrEndingScalarTypeParameters() {
        FormalTypeParameters formalTypeParameters = formalTypeParameters(invariantScalar1, variadic, invariantScalar2, invariantScalar3);
        Iterable<Type> actualTypeParameters = asList((Type)CoreTypes.BOOLEAN, CoreTypes.STRING, CoreTypes.ANY, CoreTypes.DOUBLE, CoreTypes.UNIT);
        Map<FormalTypeParameter, Type> replacementMap = formalTypeParameters.replacementMap(actualTypeParameters);
        assertThat(
            replacementMap,
            isMap(
                invariantScalar1, CoreTypes.BOOLEAN,
                variadic, CoreTypes.tupleOf(CoreTypes.STRING, CoreTypes.ANY),
                invariantScalar2, CoreTypes.DOUBLE,
                invariantScalar3, CoreTypes.UNIT
            )
        );
    }
    
    private Matcher<Map<FormalTypeParameter, Type>> isMap(FormalTypeParameter key1, Type value1) {
        Map<FormalTypeParameter, Type> expectedMap = ImmutableMap.of(key1, value1);
        return Matchers.is(expectedMap);
    }
    
    private Matcher<Map<FormalTypeParameter, Type>> isMap(FormalTypeParameter key1, Type value1, FormalTypeParameter key2, Type value2) {
        Map<FormalTypeParameter, Type> expectedMap = ImmutableMap.of(key1, value1, key2, value2);
        return Matchers.is(expectedMap);
    }
    
    private Matcher<Map<FormalTypeParameter, Type>> isMap(
        FormalTypeParameter key1, Type value1,
        FormalTypeParameter key2, Type value2,
        FormalTypeParameter key3, Type value3,
        FormalTypeParameter key4, Type value4
        ) {
        Map<FormalTypeParameter, Type> expectedMap = ImmutableMap.of(key1, value1, key2, value2, key3, value3, key4, value4);
        return Matchers.is(expectedMap);
    }
}
