package org.zwobble.shed.compiler;

import org.zwobble.shed.compiler.errors.HasErrors;

public class Results {
    public static boolean isSuccess(HasErrors hasErrors) {
        return hasErrors.getErrors().isEmpty();
    }
}
