package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.List;

import org.zwobble.shed.compiler.errors.CompilerError;
import org.zwobble.shed.compiler.errors.HasErrors;


public class TypeResultBuilder<T> {
    public static TypeResultBuilder<Void> typeResultBuilder() {
        return new TypeResultBuilder<Void>(null);
    }
    
    public static <T> TypeResultBuilder<T> typeResultBuilder(T value) {
        return new TypeResultBuilder<T>(value);
    }
    
    private final T value;
    private final List<CompilerError> errors;
    
    private TypeResultBuilder(T value) {
        this(value, new ArrayList<CompilerError>());
    }
    
    private TypeResultBuilder(T value, List<CompilerError> errors) {
        this.value = value;
        this.errors = errors;
    }
    
    public void addError(CompilerError error) {
        errors.add(error);
    }
    
    public void addErrors(HasErrors additionalErrors) {
        errors.addAll(additionalErrors.getErrors());
    }
    
    public TypeResult<T> build() {
        if (errors.isEmpty()) {
            return TypeResults.success(value);
        } else {
            return TypeResults.failure(value, errors);
        }
    }
    
    public <R> TypeResultWithValue<R> buildWithValue(R value) {
        if (errors.isEmpty()) {
            return TypeResults.success(value);
        } else {
            return TypeResults.failure(value, errors);
        }
    }
}
