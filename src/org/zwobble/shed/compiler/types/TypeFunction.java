package org.zwobble.shed.compiler.types;

import java.util.List;

public interface TypeFunction extends Type {
    List<FormalTypeParameter> getFormalTypeParameters();
}
