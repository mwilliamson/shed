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
        return new TypeResult<Void>(Option.<Void>none(), Collections.<CompilerError>emptyList());
    }
    
    public static <T> TypeResult<T> success(T value) {
        return new TypeResult<T>(Option.some(value), Collections.<CompilerError>emptyList());
    }
    
    public static <T> TypeResult<T> failure(List<CompilerError> errors) {
        return new TypeResult<T>(Option.<T>none(), errors);
    }
    
    public static <T> TypeResult<T> failure(CompilerError error) {
        return failure(asList(error));
    }
    
    public static <T> TypeResult<T> failure(T value, List<CompilerError> errors) {
        return new TypeResult<T>(Option.some(value), errors);
    }
    
    public static <T> TypeResult<T> failure(T value, CompilerError error) {
        return failure(value, asList(error));
    }
    
    public static <T> TypeResult<List<T>> combine(Iterable<? extends TypeResult<? extends T>> results) {
        List<CompilerError> errors = new ArrayList<CompilerError>();
        for (TypeResult<? extends T> result : results) {
            errors.addAll(result.getErrors());
        }
        return new TypeResult<List<T>>(Options.combine(valuesFrom(results)), errors);
    }
    
    private static <T> Iterable<Option<? extends T>> valuesFrom(Iterable<? extends TypeResult<? extends T>> results) {
        List<Option<? extends T>> values = new ArrayList<Option<? extends T>>();
        for (TypeResult<? extends T> result : results) {
            values.add(result.value);
        }
        return values;
    }
    
    private final Option<T> value;
    private final List<CompilerError> errors;
    
    private TypeResult(Option<T> value, List<CompilerError> errors) {
        this.value = value;
        this.errors = errors;
    }
    
    public T get() {
        return value.get();
    }
    
    public Option<T> asOption() {
        return value;
    }
    
    @Override
    public List<CompilerError> getErrors() {
        return errors;
    }
    
    public boolean isSuccess() {
        return errors.isEmpty();
    }
    
    public boolean hasValue() {
        return value.hasValue();
    }
    
    public TypeResult<T> orElse(T elseValue) {
        if (hasValue()) {
            return this;
        } else {
            return new TypeResult<T>(Option.some(elseValue), errors);
        }
    }
    
    public <R> TypeResult<R> ifValueThen(Function<T, TypeResult<R>> function) {
        if (hasValue()) {
            TypeResult<R> result = function.apply(get());
            return thenResult(result);
        } else {
            return new TypeResult<R>(Option.<R>none(), errors);
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
        return new TypeResult<R>(value, newErrors);
    }
}
