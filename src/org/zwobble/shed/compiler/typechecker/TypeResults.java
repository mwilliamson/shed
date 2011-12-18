package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.List;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.Options;
import org.zwobble.shed.compiler.errors.CompilerError;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.typechecker.TypeResultWithValue.typeResultWithValue;
import static org.zwobble.shed.compiler.typechecker.TypeResultWithoutValue.typeResultWithoutValue;

public class TypeResults {
    public static <T> TypeResultWithValue<T> build(T value, Iterable<CompilerError> errors) {
        return TypeResultWithValue.build(value, errors);
    }
    
    public static <T> TypeResultWithValue<T> success(T value) {
        return TypeResultWithValue.success(value);
    }
    
    public static TypeResultWithoutValue<Void> success() {
        return TypeResultWithoutValue.success();
    }
    
    public static <T> TypeResultWithValue<T> failure(T value, List<CompilerError> errors) {
        return TypeResultWithValue.failure(value, errors);
    }
    
    public static <T> TypeResultWithValue<T> failure(T value, CompilerError error) {
        return failure(value, asList(error));
    }
    
    public static <T> TypeResultWithoutValue<T> failure(List<CompilerError> errors) {
        return TypeResultWithoutValue.failure(errors);
    }
    
    public static <T> TypeResultWithoutValue<T> failure(CompilerError error) {
        return TypeResultWithoutValue.failure(error);
    }
    
    public static <T> TypeResult<List<T>> combine(Iterable<? extends TypeResult<? extends T>> results) {
        List<CompilerError> errors = new ArrayList<CompilerError>();
        for (TypeResult<? extends T> result : results) {
            errors.addAll(result.getErrors());
        }
        Option<List<T>> value = Options.combine(valuesFrom(results));
        if (value.hasValue()) {
            return typeResultWithValue(value.get(), errors);
        } else {
            return typeResultWithoutValue(errors);
        }
    }
    
    private static <T> Iterable<Option<? extends T>> valuesFrom(Iterable<? extends TypeResult<? extends T>> results) {
        List<Option<? extends T>> values = new ArrayList<Option<? extends T>>();
        for (TypeResult<? extends T> result : results) {
            values.add(result.asOption());
        }
        return values;
    }
}
