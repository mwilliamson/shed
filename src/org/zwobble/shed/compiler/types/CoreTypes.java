package org.zwobble.shed.compiler.types;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.IntRange.range;

public class CoreTypes {
    public static final Type BOOLEAN = new ScalarType(Collections.<String>emptyList(), "Boolean");
    public static final Type NUMBER = new ScalarType(Collections.<String>emptyList(), "Number");
    public static final Type STRING = new ScalarType(Collections.<String>emptyList(), "String");
    public static final Type UNIT = new ScalarType(Collections.<String>emptyList(), "Unit");
    
    public static final TypeFunction CLASS = new TypeFunction(Collections.<String>emptyList(), "Class", asList(new FormalTypeParameter("C")));
    
    public static TypeApplication classOf(Type type) {
        return new TypeApplication(CLASS, asList(type));
    }
    
    private static Map<Integer, TypeFunction> functionTypes = new HashMap<Integer, TypeFunction>();
    
    public static TypeFunction functionType(int arguments) {
        if (!functionTypes.containsKey(arguments)) {
            List<FormalTypeParameter> formalTypeParameters = newArrayList(transform(range(arguments), toFormalTypeParameter()));
            formalTypeParameters.add(new FormalTypeParameter("TResult"));
            TypeFunction functionType = new TypeFunction(Collections.<String>emptyList(), "Function" + arguments, formalTypeParameters);
            functionTypes.put(arguments, functionType);
        }
        return functionTypes.get(arguments);
    }

    private static Function<Integer, FormalTypeParameter> toFormalTypeParameter() {
        return new Function<Integer, FormalTypeParameter>() {
            @Override
            public FormalTypeParameter apply(Integer input) {
                return new FormalTypeParameter("T" + (input + 1));
            }
        };
    }
}
