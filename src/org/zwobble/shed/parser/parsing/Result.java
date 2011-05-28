package org.zwobble.shed.parser.parsing;

import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import static java.util.Arrays.asList;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Result<T> {
    public static <T> Result<T> success(T value) {
        return new Result<T>(value, Collections.<Error>emptyList(), false);
    }

    public static <T> Result<T> failure(List<Error> errors) {
        return new Result<T>(null, errors, false);
    }

    public static <T> Result<T> failure(Error... errors) {
        return failure(asList(errors));
    }

    public static <T> Result<T> fatal(List<Error> errors) {
        return new Result<T>(null, errors, true);
    }

    public static <T> Result<T> fatal(Error... errors) {
        return failure(asList(errors));
    }
    
    private final T value;
    private final List<Error> errors;
    private final boolean isFatal;
    
    public boolean anyErrors() {
        return !errors.isEmpty();
    }
    
    public <U> Result<U> changeValue(U value) {
        return new Result<U>(value, errors, isFatal);
    }
    
    public <U> Result<U> toFatal(U value) {
        return new Result<U>(value, errors, true);
    }
    
    public T get() {
        return value;
    }
    
    public boolean isFatal() {
        return isFatal;
    }
}
