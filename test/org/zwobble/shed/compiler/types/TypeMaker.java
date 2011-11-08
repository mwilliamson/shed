package org.zwobble.shed.compiler.types;

import java.util.Map;
import java.util.Set;

import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.ValueInfo;

import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Property;
import com.natpryce.makeiteasy.PropertyLookup;

import static org.zwobble.shed.compiler.types.Members.members;

import static org.zwobble.shed.compiler.types.Interfaces.interfaces;

import static com.natpryce.makeiteasy.Property.newProperty;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;

public class TypeMaker {
    public static final Property<ScalarType, Set<Type>> superTypes = newProperty();
    public static final Property<ScalarType, Map<String, ValueInfo>> members = newProperty();
    
    public static Instantiator<ClassType> classType(final StaticContext context) {
        return new Instantiator<ClassType>() {
            @Override
            public ClassType instantiate(PropertyLookup<ClassType> lookup) {
                ClassType classType = new ClassType(fullyQualifiedName());
                context.addInfo(classType, buildTypeInfo(lookup));
                return classType;
            }
        };
    }
    
    public static Instantiator<InterfaceType> interfaceType(final StaticContext context) {
        return new Instantiator<InterfaceType>() {
            @Override
            public InterfaceType instantiate(PropertyLookup<InterfaceType> lookup) {
                InterfaceType interfaceType = new InterfaceType(fullyQualifiedName());
                context.addInfo(interfaceType, buildTypeInfo(lookup));
                return interfaceType;
            }
        };
    }

    private static ScalarTypeInfo buildTypeInfo(PropertyLookup<? extends ScalarType> lookup) {
        return new ScalarTypeInfo(lookup.valueOf(superTypes, interfaces()), lookup.valueOf(members, members()));
    }
}
