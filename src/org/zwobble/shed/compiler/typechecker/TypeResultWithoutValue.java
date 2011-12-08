package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.errors.CompilerError;
import org.zwobble.shed.compiler.errors.HasErrors;

import com.google.common.base.Function;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.typechecker.TypeResultWithValue.typeResultWithValue;

@ToString
@EqualsAndHashCode
public class TypeResultWithoutValue<T> implements TypeResult<T> {
    public static TypeResultWithoutValue<Void> success() {
        return new TypeResultWithoutValue<Void>(Collections.<CompilerError>emptyList());
    }
    
    public static <T> TypeResultWithoutValue<T> failure(List<CompilerError> errors) {
        return new TypeResultWithoutValue<T>(errors);
    }
    
    public static <T> TypeResultWithoutValue<T> failure(CompilerError error) {
        return failure(asList(error));
    }
    
    public static <T> TypeResult<T> typeResultWithoutValue(List<CompilerError> errors) {
        return new TypeResultWithoutValue<T>(errors);
    }
    
    private final List<CompilerError> errors;
    
    private TypeResultWithoutValue(List<CompilerError> errors) {
        this.errors = errors;
    }
    
    public T getOrThrow() {
        throw new RuntimeException("No value");
    }
    
    public Option<T> asOption() {
        return Option.none();
    }
    
    @Override
    public List<CompilerError> getErrors() {
        return errors;
    }
    
    public boolean hasValue() {
        return false;
    }
    
    public TypeResultWithValue<T> orElse(T elseValue) {
        return typeResultWithValue(elseValue, errors);
    }
    
    @SuppressWarnings("unchecked")
    public <R> TypeResult<R> ifValueThen(Function<T, TypeResult<R>> function) {
        return (TypeResult<R>) this;
    }
    
    public TypeResultWithoutValue<T> withErrorsFrom(HasErrors... others) {
        List<CompilerError> newErrors = new ArrayList<CompilerError>(errors);
        for (HasErrors otherResult : others) {
            newErrors.addAll(otherResult.getErrors());
        }
        return new TypeResultWithoutValue<T>(newErrors);
    }
}
