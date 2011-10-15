package org.zwobble.shed.compiler.typechecker;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.zwobble.shed.compiler.types.Type;

@ToString
@EqualsAndHashCode
public class ValueInfo {
    private static ValueInfo UNKNOWN_VALUE = new ValueInfo(null, Status.UNKNOWN);
    
    public static ValueInfo unassignableValue(Type type) {
        return new ValueInfo(type, Status.UNASSIGNABLE);
    }

    public static ValueInfo assignableValue(Type type) {
        return new ValueInfo(type, Status.ASSIGNABLE);
    }

    public static ValueInfo unknown() {
        return UNKNOWN_VALUE;
    }
    
    private final Type type;
    private final Status status;
    
    private ValueInfo(Type type, Status status) {
        this.type = type;
        this.status = status;
    }
    
    public Type getType() {
        return type;
    }
    
    public boolean isAssignable() {
        if (status == Status.UNKNOWN) {
            throw new UnsupportedOperationException();
        }
        return status == Status.ASSIGNABLE;
    }
    
    private static enum Status {
        ASSIGNABLE,
        UNASSIGNABLE,
        UNKNOWN
    }
}
