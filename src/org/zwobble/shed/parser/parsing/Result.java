package org.zwobble.shed.parser.parsing;

import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Result<T> {
    public static <T> Result<T> success(T value) {
        return new Result<T>(value, Collections.<CompilerError>emptyList(), Type.SUCCESS);
    }
    
    private final T value;
    private final List<CompilerError> errors;
    private final Type type;
    
    public boolean anyErrors() {
        return !errors.isEmpty();
    }
    
    public <U> Result<U> changeValue(U value) {
        return new Result<U>(value, errors, type);
    }
    
    public <U> Result<U> toType(U value, Type type) {
        return new Result<U>(value, errors, type);
    }
    
    public T get() {
        return value;
    }
    
    public List<CompilerError> getErrors() {
        return errors;
    }
    
    public static enum Type {
        SUCCESS,
        NO_MATCH,
        ERROR_RECOVERED,
        ERROR_RECOVERED_WITH_VALUE,
        FATAL
    }
    
    public boolean isSuccess() {
        return type == Type.SUCCESS;
    }
    
    public boolean hasValue() {
        return type == Type.SUCCESS || type == Type.ERROR_RECOVERED_WITH_VALUE;
    }

    public boolean ruleDidFinish() {
        return type == Type.SUCCESS || type == Type.ERROR_RECOVERED || type == Type.ERROR_RECOVERED_WITH_VALUE;
    }
    public boolean noMatch() {
        return type == Type.NO_MATCH;
    }
    public boolean isFatal() {
        return type == Type.FATAL;
    }
}
