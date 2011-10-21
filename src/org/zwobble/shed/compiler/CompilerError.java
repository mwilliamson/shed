package org.zwobble.shed.compiler;


public interface CompilerError {
    CompilerErrorDescription getDescription();
    String describe();
}
