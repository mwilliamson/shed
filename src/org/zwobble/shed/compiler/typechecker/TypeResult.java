package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.HasErrors;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.Options;

import com.google.common.base.Function;

import static java.util.Arrays.asList;

@ToString
@EqualsAndHashCode
public class TypeResult<T> implements HasErrors {
    public static TypeResult<Void> success() {
        return new TypeResult<Void>(true, Option.<Void>none(), Collections.<CompilerError>emptyList());
    }
    
    public static <T> TypeResult<T> success(T value) {
        return new TypeResult<T>(true, Option.some(value), Collections.<CompilerError>emptyList());
    }
    
    public static <T> TypeResult<T> failure(List<CompilerError> errors) {
        return new TypeResult<T>(false, Option.<T>none(), errors);
    }
    
    public static <T> TypeResult<T> failure(CompilerError error) {
        return failure(asList(error));
    }
    
    public static <T> TypeResult<T> failure(T value, List<CompilerError> errors) {
        return new TypeResult<T>(false, Option.some(value), errors);
    }
    
    public static <T> TypeResult<T> failure(T value, CompilerError error) {
        return failure(value, asList(error));
    }
    
    public static <T> TypeResult<List<T>> combine(Iterable<? extends TypeResult<? extends T>> results) {
        List<CompilerError> errors = new ArrayList<CompilerError>();
        boolean success = true;
        for (TypeResult<? extends T> result : results) {
            success &= result.success;
            errors.addAll(result.getErrors());
        }
        return new TypeResult<List<T>>(success, Options.combine(valuesFrom(results)), errors);
    }
    
    private static <T> Iterable<Option<? extends T>> valuesFrom(Iterable<? extends TypeResult<? extends T>> results) {
        List<Option<? extends T>> values = new ArrayList<Option<? extends T>>();
        for (TypeResult<? extends T> result : results) {
            values.add(result.value);
        }
        return values;
    }
    
    private final boolean success;
    private final Option<T> value;
    private final List<CompilerError> errors;
    
    private TypeResult(boolean success, Option<T> value, List<CompilerError> errors) {
        this.success = success;
        this.value = value;
        this.errors = errors;
    }
    
    public T get() {
        return value.get();
    }
    
    @Override
    public List<CompilerError> getErrors() {
        return errors;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public boolean hasValue() {
        return value.hasValue();
    }
    
    public TypeResult<T> orElse(T elseValue) {
        if (hasValue()) {
            return this;
        } else {
            return new TypeResult<T>(success, Option.some(elseValue), errors);
        }
    }
    
    public <R> TypeResult<R> ifValueThen(Function<T, TypeResult<R>> function) {
        if (hasValue()) {
            TypeResult<R> result = function.apply(get());
            return thenResult(result);
        } else {
            return new TypeResult<R>(success, Option.<R>none(), errors);
        }
    }
    
    public TypeResult<T> withErrorsFrom(TypeResult<?>... others) {
        TypeResult<T> result = this;
        for (TypeResult<?> otherResult : others) {
            result = result.thenResult(otherResult, value);
        }
        return result;
    }
    
    private <R> TypeResult<R> thenResult(TypeResult<R> result) {
        return thenResult(result, result.value);
    }
    
    private <R> TypeResult<R> thenResult(TypeResult<?> result, Option<R> value) {
        List<CompilerError> newErrors = new ArrayList<CompilerError>();
        newErrors.addAll(errors);
        newErrors.addAll(result.errors);
        return new TypeResult<R>(success && result.success, value, newErrors);
    }
}
