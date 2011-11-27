package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.CompilerError;
import java.util.List;

import org.zwobble.shed.compiler.HasErrors;
import org.zwobble.shed.compiler.Option;

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
