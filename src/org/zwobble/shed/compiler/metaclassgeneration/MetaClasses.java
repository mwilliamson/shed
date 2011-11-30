package org.zwobble.shed.compiler.metaclassgeneration;

import lombok.RequiredArgsConstructor;

import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.ScalarType;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

@RequiredArgsConstructor(staticName="metaClasses")
public class MetaClasses {
    private final BiMap<ScalarType, ClassType> typesToMetaClasses = HashBiMap.<ScalarType, ClassType>create();
    
    public ClassType metaClassOf(ScalarType type) {
        if (!typesToMetaClasses.containsKey(type)) {
            typesToMetaClasses.put(type, createMetaClass(type));
        }
        return typesToMetaClasses.get(type);
    }
    
    public ScalarType getTypeFromMetaClass(ClassType metaClass) {
        return typesToMetaClasses.inverse().get(metaClass);
    }
    
    private ClassType createMetaClass(ScalarType type) {
        return new ClassType(type.getFullyQualifiedName().extend("$Meta"));
    }
}
