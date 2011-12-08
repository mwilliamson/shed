package org.zwobble.shed.compiler;

public class CompilationDataKey<T> {
    public static <T> CompilationDataKey<T> key() {
        return new CompilationDataKey<T>();
    }
}
