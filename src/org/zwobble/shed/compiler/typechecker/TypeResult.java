package org.zwobble.shed.compiler.typechecker;

import java.util.Collections;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.zwobble.shed.compiler.HasErrors;
import org.zwobble.shed.compiler.parsing.CompilerError;

@ToString
@EqualsAndHashCode
public class TypeResult<T> implements HasErrors {
    public static <T> TypeResult<T> success(T value) {
        return new TypeResult<T>(true, value, Collections.<CompilerError>emptyList());
    }
    
    public static <T> TypeResult<T> failure(List<CompilerError> errors) {
        return new TypeResult<T>(false, null, errors);
    }
    
    private final boolean success;
    private final T value;
    private final List<CompilerError> errors;
    
    private TypeResult(boolean success, T value, List<CompilerError> errors) {
        this.success = success;
        this.value = value;
        this.errors = errors;
    }
    
    public T get() {
        return value;
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
