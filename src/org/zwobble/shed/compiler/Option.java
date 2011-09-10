package org.zwobble.shed.compiler;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class Option<T> {
    private final boolean hasValue;
    private final T value;

    public static <T> Option<T> some(T value) {
        return new Option<T>(true, value);
    }
    
    public static<T> Option<T> none(Class<T> clazz) {
        return none();
    }
    
    public static<T> Option<T> none() {
        return new Option<T>(false, null);
    }
    
    public boolean hasValue() {
        return hasValue;
    }
    
    public T get() {
        if (!hasValue) {
            throw new RuntimeException("Option has no value");
        }
        return value;
    }
    
    private Option(boolean hasValue, T value) {
        this.hasValue = hasValue;
        this.value = value;
    }
}
