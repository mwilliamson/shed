package org.zwobble.shed.compiler.types;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.IntRange.range;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.types.ParameterisedType.parameterisedType;

public class CoreTypes {
    public static final ScalarType BOOLEAN = coreType("Boolean");
    public static final ScalarType DOUBLE = coreType("Double");
    public static final ScalarType STRING = coreType("String");
    public static final ScalarType UNIT = coreType("Unit");
    
    private static ScalarType coreType(String name) {
        return new ClassType(fullyQualifiedName(name));
    }
    
    public static final ParameterisedType CLASS = parameterisedType(
        new InterfaceType(fullyQualifiedName("Class")),
        asList(new FormalTypeParameter("C"))
    );
    
    public static Type classOf(Type type) {
        return new TypeApplication(CLASS, asList(type));
    }
    
    private static Map<Integer, ParameterisedType> functionTypes = new HashMap<Integer, ParameterisedType>();
    private static Set<Type> baseFunctionTypes = new HashSet<Type>();
    
    public static boolean isFunction(Type type) {
        return type instanceof TypeApplication && baseFunctionTypes.contains((((TypeApplication)type).getBaseType().getBaseType()));
    }
    
    public static ParameterisedType functionType(int arguments) {
        if (!functionTypes.containsKey(arguments)) {
            InterfaceType baseType = new InterfaceType(fullyQualifiedName("Function" + arguments));
            baseFunctionTypes.add(baseType);
            
            List<FormalTypeParameter> formalTypeParameters = newArrayList(transform(range(arguments), toFormalTypeParameter()));
            formalTypeParameters.add(new FormalTypeParameter("TResult"));
            ParameterisedType functionType = parameterisedType(baseType, formalTypeParameters);
            functionTypes.put(arguments, functionType);
        }
        return functionTypes.get(arguments);
    }
    
    public static Type functionTypeOf(Type... types) {
        return functionTypeOf(asList(types));
    }
    
    public static Type functionTypeOf(List<Type> types) {
        return new TypeApplication(functionType(types.size() - 1), types);
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
