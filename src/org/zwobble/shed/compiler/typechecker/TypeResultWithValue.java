package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.errors.CompilerError;
import org.zwobble.shed.compiler.errors.HasErrors;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import static java.util.Arrays.asList;

@ToString
@EqualsAndHashCode
@AllArgsConstructor(staticName="typeResultWithValue")
public class TypeResultWithValue<T> implements TypeResult<T> {
    public static <T> TypeResultWithValue<T> build(T value, Iterable<CompilerError> errors) {
        return new TypeResultWithValue<T>(value, ImmutableList.copyOf(errors));
    }
    
    public static <T> TypeResultWithValue<T> success(T value) {
        return new TypeResultWithValue<T>(value, Collections.<CompilerError>emptyList());
    }
    
    public static <T> TypeResultWithValue<T> failure(T value, List<CompilerError> errors) {
        return new TypeResultWithValue<T>(value, errors);
    }
    
    public static <T> TypeResultWithValue<T> failure(T value, CompilerError error) {
        return failure(value, asList(error));
    }

    public static <T> TypeResultWithValue<List<T>> combine(Iterable<? extends TypeResultWithValue<? extends T>> results) {
        List<T> values = new ArrayList<T>();
        List<CompilerError> errors = new ArrayList<CompilerError>();
        for (TypeResultWithValue<? extends T> result : results) {
            values.add(result.get());
            errors.addAll(result.getErrors());
        }
        return typeResultWithValue(values, errors);
    }
    
    private final T value;
    private final List<CompilerError> errors;
    
    @Override
    public T getOrThrow() {
        return value;
    }

    public T get() {
        return value;
    }
    
    @Override
    public Option<T> asOption() {
        return Option.some(value);
    }
    
    @Override
    public List<CompilerError> getErrors() {
        return errors;
    }
    
    @Override
    public boolean hasValue() {
        return true;
    }
    
    @Override
    public TypeResultWithValue<T> orElse(T elseValue) {
        return this;
    }
    
    @Override
    public <R> TypeResult<R> ifValueThen(Function<T, TypeResult<R>> function) {
        TypeResult<R> result = function.apply(value);
        return result.withErrorsFrom(this);
    }
    
    public <R> TypeResultWithValue<R> map(Function<T, R> function) {
        R result = function.apply(value);
        return TypeResults.build(result, errors);
    }
    
    @Override
    public TypeResultWithValue<T> withErrorsFrom(HasErrors... others) {
        List<CompilerError> newErrors = new ArrayList<CompilerError>(errors);
        for (HasErrors otherResult : others) {
            newErrors.addAll(otherResult.getErrors());
        }
        return new TypeResultWithValue<T>(value, newErrors);
    }
}
