package org.zwobble.shed.parser.parsing;

import java.util.Collections;
import java.util.List;

import lombok.Data;

import static java.util.Arrays.asList;

@Data
public class Result<T> {
    public static <T> Result<T> success(T value) {
        return new Result<T>(value, Collections.<Error>emptyList());
    }

    public static <T> Result<T> failure(List<Error> errors) {
        return new Result<T>(null, errors);
    }

    public static <T> Result<T> failure(Error... errors) {
        return failure(asList(errors));
    }
    
    private final T value;
    private final List<Error> errors;
    
    public boolean anyErrors() {
        return !errors.isEmpty();
    }
    
    public <U> Result<U> changeValue(U value) {
        return new Result<U>(value, errors);
    }
    
    public T get() {
        return value;
    }
}
