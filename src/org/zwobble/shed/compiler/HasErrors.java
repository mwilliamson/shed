package org.zwobble.shed.compiler;

import java.util.List;


public interface HasErrors {
    List<? extends CompilerError> getErrors();
}
