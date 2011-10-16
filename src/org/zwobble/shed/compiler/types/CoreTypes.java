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

public class CoreTypes {
    public static final ScalarType BOOLEAN = coreType("Boolean");
    public static final ScalarType NUMBER = coreType("Number");
    public static final ScalarType STRING = coreType("String");
    public static final ScalarType UNIT = coreType("Unit");
    
    private static ScalarType coreType(String name) {
        return new ClassType(fullyQualifiedName(name));
    }
    
    public static final ParameterisedType CLASS = new ParameterisedType(
        new InterfaceType(fullyQualifiedName("Class")),
        asList(new FormalTypeParameter("C"))
    );
    
    public static InterfaceType classOf(Type type) {
        return (InterfaceType) TypeApplication.applyTypes(CLASS, asList(type));
    }
    
    private static Map<Integer, ParameterisedType> functionTypes = new HashMap<Integer, ParameterisedType>();
    private static Set<Type> baseFunctionTypes = new HashSet<Type>();
    
    public static boolean isFunction(Type type) {
        return type instanceof TypeApplication && baseFunctionTypes.contains((((TypeApplication)type).getBaseType()));
    }
    
    public static ParameterisedType functionType(int arguments) {
        if (!functionTypes.containsKey(arguments)) {
            ScalarType baseType = new InterfaceType(fullyQualifiedName("Function" + arguments));
            baseFunctionTypes.add(baseType);
            
            List<FormalTypeParameter> formalTypeParameters = newArrayList(transform(range(arguments), toFormalTypeParameter()));
            formalTypeParameters.add(new FormalTypeParameter("TResult"));
            ParameterisedType functionType = new ParameterisedType(
                baseType,
                formalTypeParameters
            );
            functionTypes.put(arguments, functionType);
        }
        return functionTypes.get(arguments);
    }
    
    public static InterfaceType functionTypeOf(Type... types) {
        // TODO: put type parameter in ParameterisedType so we know if it's parameterised over a class or interface
        return (InterfaceType) TypeApplication.applyTypes(functionType(types.length - 1), asList(types));
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
