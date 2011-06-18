package org.zwobble.shed.compiler.typechecker;

import java.util.Collections;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.zwobble.shed.compiler.HasErrors;
import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.types.Type;

@ToString
@EqualsAndHashCode
public class TypeResult implements HasErrors {
    public static TypeResult success(Type type) {
        return new TypeResult(true, type, Collections.<CompilerError>emptyList());
    }
    
    public static TypeResult failure(List<CompilerError> errors) {
        return new TypeResult(false, null, errors);
    }
    
    private final boolean success;
    private final Type type;
    private final List<CompilerError> errors;
    
    private TypeResult(boolean success, Type type, List<CompilerError> errors) {
        this.success = success;
        this.type = type;
        this.errors = errors;
    }
    
    public Type get() {
        return type;
    }
    
    @Override
    public List<CompilerError> getErrors() {
        return errors;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public boolean hasValue() {
        return success;
    }
}
