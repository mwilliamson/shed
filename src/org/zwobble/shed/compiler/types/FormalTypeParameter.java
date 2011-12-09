package org.zwobble.shed.compiler.types;

import org.zwobble.shed.compiler.types.ScalarFormalTypeParameter.Variance;

public interface FormalTypeParameter extends Type {
    Variance getVariance();
}
