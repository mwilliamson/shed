package org.zwobble.shed.parser;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
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
    
    private Option(boolean hasValue, T value) {
        this.hasValue = hasValue;
        this.value = value;
    }
}
