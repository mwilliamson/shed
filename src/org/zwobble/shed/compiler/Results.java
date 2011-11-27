package org.zwobble.shed.compiler;

public class Results {
    public static boolean isSuccess(HasErrors hasErrors) {
        return hasErrors.getErrors().isEmpty();
    }
}
