package org.zwobble.shed.compiler.types;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import static com.google.common.collect.Iterables.concat;
import static java.util.Collections.singleton;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.IntRange.range;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.types.FormalTypeParameter.contravariantFormalTypeParameter;
import static org.zwobble.shed.compiler.types.FormalTypeParameter.covariantFormalTypeParameter;
import static org.zwobble.shed.compiler.types.ParameterisedType.parameterisedType;
import static org.zwobble.shed.compiler.types.TypeApplication.applyTypes;

public class CoreTypes {
    public static final ClassType BOOLEAN = coreType("Boolean");
    public static final ClassType DOUBLE = coreType("Double");
    public static final ClassType STRING = coreType("String");
    public static final ClassType UNIT = coreType("Unit");
    
    private static ClassType coreType(String name) {
        return new ClassType(fullyQualifiedName(name));
    }
    
    public static final InterfaceType CLASS = new InterfaceType(fullyQualifiedName("Class"));
    public static final Type ANY = AnyType.ANY;
    
    private static Map<Integer, ParameterisedType> functionTypes = new HashMap<Integer, ParameterisedType>();
    private static Set<ParameterisedType> baseFunctionTypes = new HashSet<ParameterisedType>();
    
    public static ParameterisedType functionType(int arguments) {
        if (!functionTypes.containsKey(arguments)) {
            InterfaceType baseType = new InterfaceType(fullyQualifiedName("Function" + arguments));
            List<FormalTypeParameter> formalTypeParameters = newArrayList(transform(range(arguments), toContravariantFormalTypeParameter()));
            formalTypeParameters.add(covariantFormalTypeParameter("TResult"));
            ParameterisedType functionType = parameterisedType(baseType, formalTypeParameters);
            functionTypes.put(arguments, functionType);
            baseFunctionTypes.add(functionType);
        }
        return functionTypes.get(arguments);
    }
    
    public static ScalarType functionTypeOf(Type... types) {
        return functionTypeOf(asList(types));
    }
    
    public static ScalarType functionTypeOf(Iterable<Type> types) {
        return functionTypeOf(ImmutableList.copyOf(types));
    }
    
    public static ScalarType functionTypeOf(Iterable<Type> argumentTypes, Type returnType) {
        return functionTypeOf(concat(argumentTypes, singleton(returnType)));
    }
    
    public static ScalarType functionTypeOf(List<Type> types) {
        return applyTypes(functionType(types.size() - 1), types);
    }

    private static Function<Integer, FormalTypeParameter> toContravariantFormalTypeParameter() {
        return new Function<Integer, FormalTypeParameter>() {
            @Override
            public FormalTypeParameter apply(Integer input) {
                return contravariantFormalTypeParameter("T" + (input + 1));
            }
        };
    }
}
