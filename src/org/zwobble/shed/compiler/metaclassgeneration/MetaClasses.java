package org.zwobble.shed.compiler.metaclassgeneration;

import lombok.RequiredArgsConstructor;

import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

@RequiredArgsConstructor(staticName="create")
public class MetaClasses {
    private final BiMap<ScalarType, ClassType> typesToMetaClasses = HashBiMap.<ScalarType, ClassType>create();
    
    public ClassType metaClassOf(ScalarType type) {
        if (!typesToMetaClasses.containsKey(type)) {
            typesToMetaClasses.put(type, createMetaClass(type));
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
