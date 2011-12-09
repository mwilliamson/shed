package org.zwobble.shed.compiler.types;

import org.zwobble.shed.compiler.types.ScalarFormalTypeParameter.Variance;

public class VariadicFormalTypeParameter implements FormalTypeParameter {
    public static VariadicFormalTypeParameter invariant(String name) {
        return new VariadicFormalTypeParameter(name, Variance.INVARIANT);
    }
    
    public static VariadicFormalTypeParameter covariant(String name) {
        return new VariadicFormalTypeParameter(name, Variance.COVARIANT);
    }
    
    public static VariadicFormalTypeParameter contravariant(String name) {
        return new VariadicFormalTypeParameter(name, Variance.CONTRAVARIANT);
    }
    
    private final String name;
    private final Variance variance;

    private VariadicFormalTypeParameter(String name, Variance variance) {
        this.name = name;
        this.variance = variance;
    }
    
    @Override
    public String shortName() {
        return "*" + name;
    }

    @Override
    public Variance getVariance() {
        return variance;
    }
    
    @Override
    public String toString() {
        return shortName();
    }
}
