package org.zwobble.shed.compiler.errors;

import java.util.List;



public interface HasErrors {
    List<? extends CompilerError> getErrors();
}
