package org.zwobble.shed.compiler.metaclassgeneration;

import lombok.AllArgsConstructor;

import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.ScalarType;

import com.google.common.collect.BiMap;

@AllArgsConstructor(staticName="metaClasses")
public class MetaClasses {
    private final BiMap<ScalarType, ClassType> typesToMetaClasses;
    
    public ClassType metaClassOf(ScalarType type) {
        return typesToMetaClasses.get(type);
    }
    public ScalarType getTypeFromMetaClass(ClassType metaClass) {
        return typesToMetaClasses.inverse().get(metaClass);
    }
}
