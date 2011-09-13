package org.zwobble.shed.compiler;

import org.zwobble.shed.compiler.parsing.SourceRange;

public interface CompilerError {
    SourceRange getLocation();
    String getDescription();
}
