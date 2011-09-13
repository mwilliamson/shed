package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.Function0;
import org.zwobble.shed.compiler.HasErrors;

import com.google.common.base.Function;

@ToString
@EqualsAndHashCode
public class TypeResult<T> implements HasErrors {
    public static TypeResult<Void> success() {
        return success(null);
    }
    
    public static <T> TypeResult<T> success(T value) {
        return new TypeResult<T>(true, true, value, Collections.<CompilerError>emptyList());
    }
    
    public static <T> TypeResult<T> failure(List<? extends CompilerError> errors) {
        return new TypeResult<T>(false, false, null, errors);
    }
    
    public static <T> TypeResult<T> failure(T value, List<? extends CompilerError> errors) {
        return new TypeResult<T>(false, true, value, errors);
    }
    
    public static <T> TypeResult<List<T>> combine(Iterable<? extends TypeResult<? extends T>> results) {
        List<T> values = new ArrayList<T>();
        List<CompilerError> errors = new ArrayList<CompilerError>();
        boolean success = true;
        boolean hasValue = true;
        for (TypeResult<? extends T> result : results) {
            success &= result.success;
            hasValue &= result.hasValue;
            errors.addAll(result.getErrors());
            values.add(result.value);
        }
        return new TypeResult<List<T>>(success, hasValue, values, errors);
        
    }
    
    private final boolean success;
    private final boolean hasValue;
    private final T value;
    private final List<? extends CompilerError> errors;
    
    private TypeResult(boolean success, boolean hasValue, T value, List<? extends CompilerError> errors) {
        this.success = success;
        this.hasValue = hasValue;
        this.value = value;
        this.errors = errors;
    }
    
    public T get() {
        return value;
    }
    
    @Override
    public List<? extends CompilerError> getErrors() {
        return errors;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public boolean hasValue() {
        return hasValue;
    }
    
    public <R> TypeResult<R> then(Function0<TypeResult<R>> function) {
        TypeResult<R> result = function.apply();
        return thenResult(result);
    }
    
    public <R> TypeResult<R> use(Function<T, TypeResult<R>> function) {
        if (hasValue()) {
            return function.apply(value);
        } else {
            return new TypeResult<R>(false, false, null, Collections.<CompilerError>emptyList());
        }
    }
    
    
    public <R> TypeResult<R> ifValueThen(Function<T, TypeResult<R>> function) {
        if (hasValue()) {
            TypeResult<R> result = function.apply(value);
            return thenResult(result);
        } else {
            return new TypeResult<R>(success, false, null, errors);
        }
    }
    
    public TypeResult<T> withErrorsFrom(TypeResult<?> other) {
        return thenResult(other, hasValue, value);
    }
    
    private <R> TypeResult<R> thenResult(TypeResult<R> result) {
        return thenResult(result, result.hasValue, result.value);
    }
    
    private <R> TypeResult<R> thenResult(TypeResult<?> result, boolean hasValue, R value) {
        List<CompilerError> newErrors = new ArrayList<CompilerError>();
        newErrors.addAll(errors);
        newErrors.addAll(result.errors);
        return new TypeResult<R>(success && result.success, hasValue, value, newErrors);
    }
}
