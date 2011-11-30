package org.zwobble.shed.compiler.metaclassgeneration;

import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.ScalarType;

import com.google.common.collect.ImmutableBiMap;

public class MetaClassGenerator {
    public MetaClasses generate(Iterable<ScalarType> types) {
        ImmutableBiMap.Builder<ScalarType, ClassType> typesToMetaClasses = ImmutableBiMap.builder();
        for (ScalarType type : types) {
            typesToMetaClasses.put(type, new ClassType(type.getFullyQualifiedName().extend("$Meta")));
        }
        return MetaClasses.metaClasses(typesToMetaClasses.build());
    }
}
