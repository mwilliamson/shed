package org.zwobble.shed.compiler.metaclassgeneration;

import lombok.RequiredArgsConstructor;

import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.FormalTypeParameter;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

@RequiredArgsConstructor(staticName="create")
public class MetaClasses {
    private final BiMap<ScalarType, ClassType> typesToMetaClasses = HashBiMap.<ScalarType, ClassType>create();
    
    public Type metaClassOf(Type type) {
        if (type instanceof FormalTypeParameter) {
            // TODO: test
            return CoreTypes.CLASS;
        }
        if (!typesToMetaClasses.containsKey(type) && type instanceof ScalarType) {
            ScalarType scalarType = (ScalarType)type;
            typesToMetaClasses.put(scalarType, createMetaClass(scalarType));
        }
        return typesToMetaClasses.get(type);
    }
    
    public Type getTypeFromMetaClass(Type metaClass) {
        return typesToMetaClasses.inverse().get(metaClass);
    }
    
    public boolean isMetaClass(Type type) {
        return typesToMetaClasses.containsValue(type);
    }
    
    private ClassType createMetaClass(ScalarType type) {
        return new ClassType(type.getFullyQualifiedName().extend("$Meta"));
    }
}
