package org.zwobble.shed.compiler.typechecker;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.Option.some;

@ToString
@EqualsAndHashCode
public class ValueInfo {
    public static ValueInfo unassignableValue(Type type, ShedValue value) {
        return new ValueInfo(type, Status.UNASSIGNABLE, some(value));
    }
    
    public static ValueInfo unassignableValue(Type type) {
        return new ValueInfo(type, Status.UNASSIGNABLE, Option.<ShedValue>none());
    }

    public static ValueInfo assignableValue(Type type) {
        return new ValueInfo(type, Status.ASSIGNABLE, Option.<ShedValue>none());
    }
    
    private final Type type;
    private final Status status;
    private final Option<ShedValue> value;
    
    private ValueInfo(Type type, Status status, Option<ShedValue> value) {
        this.type = type;
        this.status = status;
        this.value = value;
    }
    
    public Type getType() {
        return type;
    }
    
    public Option<ShedValue> getValue() {
        return value;
    }
    
    public boolean isAssignable() {
        return status == Status.ASSIGNABLE;
    }

    private static enum Status {
        ASSIGNABLE,
        UNASSIGNABLE
    }
}
