package org.zwobble.shed.compiler.typechecker;

import java.util.List;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.errors.CompilerError;
import org.zwobble.shed.compiler.errors.HasErrors;

import com.google.common.base.Function;

public interface TypeResult<T> extends HasErrors {
    List<CompilerError> getErrors();
    TypeResultWithValue<T> orElse(T elseValue);
    T getOrThrow();
    Option<T> asOption();
    boolean hasValue();
    <R> TypeResult<R> ifValueThen(Function<T, TypeResult<R>> function);
    TypeResult<T> withErrorsFrom(HasErrors... others);
}
