package org.zwobble.shed.compiler.types;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class ScalarFormalTypeParameter implements FormalTypeParameter {
    public static FormalTypeParameter invariantFormalTypeParameter(String name) {
        return new ScalarFormalTypeParameter(name, Variance.INVARIANT);
    }
    
    public static FormalTypeParameter covariantFormalTypeParameter(String name) {
        return new ScalarFormalTypeParameter(name, Variance.COVARIANT);
    }

    public static FormalTypeParameter contravariantFormalTypeParameter(String name) {
        return new ScalarFormalTypeParameter(name, Variance.CONTRAVARIANT);
    }
    
    private ScalarFormalTypeParameter(String name, Variance variance) {
        this.name = name;
        this.variance = variance;
    }
    
    private final String name;
    private final Variance variance;
    
    @Override
    public String shortName() {
        return name;
    }
    
    public static enum Variance {
        INVARIANT,
        COVARIANT,
        CONTRAVARIANT
    }
}
