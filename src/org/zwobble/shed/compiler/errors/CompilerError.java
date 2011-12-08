package org.zwobble.shed.compiler.errors;


public interface CompilerError {
    CompilerErrorDescription getDescription();
    String describe();
}
