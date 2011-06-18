package org.zwobble.shed.compiler;

import java.util.List;

import org.zwobble.shed.compiler.parsing.CompilerError;

public interface HasErrors {
    List<CompilerError> getErrors();
}
