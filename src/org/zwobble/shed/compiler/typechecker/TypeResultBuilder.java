package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.List;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.HasErrors;


public class TypeResultBuilder<T> {
    public static TypeResultBuilder<Void> typeResultBuilder() {
        return new TypeResultBuilder<Void>(null);
    }
    
    public static <T> TypeResultBuilder<T> typeResultBuilder(T value) {
        return new TypeResultBuilder<T>(value);
    }
    
    private final T value;
    private final List<CompilerError> errors = new ArrayList<CompilerError>();
    
    private TypeResultBuilder(T value) {
        this.value = value;
    }
    
    public void addError(CompilerError error) {
        errors.add(error);
    }
    
    public void addErrors(HasErrors additionalErrors) {
        errors.addAll(additionalErrors.getErrors());
    }
    
    public TypeResult<T> build() {
        if (errors.isEmpty()) {
            return TypeResult.success(value);
        } else {
            return TypeResult.failure(value, errors);
        }
    }
}
