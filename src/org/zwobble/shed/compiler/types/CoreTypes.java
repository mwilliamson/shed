package org.zwobble.shed.compiler.types;

import java.util.List;

import com.google.common.collect.ImmutableList;

import static com.google.common.collect.Iterables.concat;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.types.FormalTypeParameters.formalTypeParameters;
import static org.zwobble.shed.compiler.types.ParameterisedType.parameterisedType;
import static org.zwobble.shed.compiler.types.ScalarFormalTypeParameter.covariantFormalTypeParameter;
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

    public static final ParameterisedType TUPLE = parameterisedType(
        new InterfaceType(fullyQualifiedName("Tuple")),
        formalTypeParameters(VariadicFormalTypeParameter.covariant("T"))
    );

    public static Type tupleOf(Type... types) {
        return tupleOf(asList(types));
    }
    
    public static Type tupleOf(Iterable<Type> types) {
        return applyTypes(TUPLE, ImmutableList.copyOf(types));
    }
    
    public static final ParameterisedType FUNCTION = parameterisedType(
        new InterfaceType(fullyQualifiedName("Function")),
        formalTypeParameters(VariadicFormalTypeParameter.contravariant("T"), covariantFormalTypeParameter("TResult"))
    );
    
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
        return applyTypes(FUNCTION, types);
    }
}
