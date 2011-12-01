package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Function;

import static org.zwobble.shed.compiler.typechecker.TypeResults.success;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;

public class ValueInfos {
    public static Function<Type, TypeResult<ValueInfo>> toUnassignableValueInfo() {
        return new Function<Type, TypeResult<ValueInfo>>() {
            @Override
            public TypeResult<ValueInfo> apply(Type input) {
                return success(unassignableValue(input));
            }
        };
    }
}
