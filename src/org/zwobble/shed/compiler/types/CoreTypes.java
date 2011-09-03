package org.zwobble.shed.compiler.types;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.IntRange.range;

public class CoreTypes {
    public static final Type BOOLEAN = coreType("Boolean");
    public static final Type NUMBER = coreType("Number");
    public static final Type STRING = coreType("String");
    public static final Type UNIT = coreType("Unit");
    public static final Type OBJECT = new InterfaceType(Collections.<String>emptyList(), "Object", ImmutableMap.<String, Type>of());
    
    private static Type coreType(String name) {
        return new ClassType(
            Collections.<String>emptyList(),
            name,
            Collections.<InterfaceType>emptySet(),
            Collections.<String, Type>emptyMap()
        );
    }
    
    public static final ParameterisedType CLASS = new ParameterisedType(
        coreType("Class"),
        asList(new FormalTypeParameter("C"))
    );
    
    public static TypeApplication classOf(Type type) {
        return new TypeApplication(CLASS, asList(type));
    }
    
    private static Map<Integer, ParameterisedType> functionTypes = new HashMap<Integer, ParameterisedType>();
    
    public static boolean isFunction(Type type) {
        return type instanceof TypeApplication && functionTypes.containsValue((((TypeApplication)type).getTypeFunction()));
    }
    
    public static ParameterisedType functionType(int arguments) {
        if (!functionTypes.containsKey(arguments)) {
            List<FormalTypeParameter> formalTypeParameters = newArrayList(transform(range(arguments), toFormalTypeParameter()));
            formalTypeParameters.add(new FormalTypeParameter("TResult"));
            ParameterisedType functionType = new ParameterisedType(
                coreType("Function" + arguments),
                formalTypeParameters
            );
            functionTypes.put(arguments, functionType);
        }
        return functionTypes.get(arguments);
    }
    
    public static TypeApplication functionTypeOf(Type... types) {
        return new TypeApplication(functionType(types.length - 1), asList(types));
    }

    private static Function<Integer, FormalTypeParameter> toFormalTypeParameter() {
        return new Function<Integer, FormalTypeParameter>() {
            @Override
            public FormalTypeParameter apply(Integer input) {
                return new FormalTypeParameter("T" + (input + 1));
            }
        };
    }

    public static Type typeFunction(ParameterisedType typeFunction) {
        return null;
    }
}
