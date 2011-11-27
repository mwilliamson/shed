package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.HasErrors;
import org.zwobble.shed.compiler.Option;

import com.google.common.base.Function;

import static java.util.Arrays.asList;

@ToString
@EqualsAndHashCode
@AllArgsConstructor(staticName="typeResultWithValue")
public class TypeResultWithValue<T> implements TypeResult<T> {
    public static <T> TypeResultWithValue<T> success(T value) {
        return new TypeResultWithValue<T>(value, Collections.<CompilerError>emptyList());
    }
    
    public static <T> TypeResultWithValue<T> failure(T value, List<CompilerError> errors) {
        return new TypeResultWithValue<T>(value, errors);
    }
    
    public static <T> TypeResultWithValue<T> failure(T value, CompilerError error) {
        return failure(value, asList(error));
    }
    
    private final T value;
    private final List<CompilerError> errors;
    
    @Override
    public T getOrThrow() {
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
    
    @Override
    public TypeResultWithValue<T> withErrorsFrom(HasErrors... others) {
        List<CompilerError> newErrors = new ArrayList<CompilerError>(errors);
        for (HasErrors otherResult : others) {
            newErrors.addAll(otherResult.getErrors());
        }
        return new TypeResultWithValue<T>(value, newErrors);
    }

}
