package org.zwobble.shed.compiler.typechecker;

import java.util.Map;

import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.ParameterisedType;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class CoreModule {
    public static final Map<String, Type> VALUES;
    
    static {
        Builder<String, Type> builder = ImmutableMap.<String, Type>builder();
        builder.put("String", CoreTypes.classOf(CoreTypes.STRING));
        builder.put("Number", CoreTypes.classOf(CoreTypes.NUMBER));
        builder.put("Boolean", CoreTypes.classOf(CoreTypes.BOOLEAN));
        builder.put("Unit", CoreTypes.classOf(CoreTypes.UNIT));
        
        for (int i = 0; i < 20; i += 1) {
            ParameterisedType functionType = CoreTypes.functionType(i);
            // TODO: remove assumption that the base type is a ClassType
            builder.put(((ClassType)functionType.getBaseType()).getName(), functionType);
        }
        VALUES = builder.build();
    }
}
