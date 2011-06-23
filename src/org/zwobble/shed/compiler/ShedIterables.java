package org.zwobble.shed.compiler;


public class ShedIterables {
    public static <T, R> R foldLeft(R initial, Iterable<T> values, Function2<R, T, R> function) {
        R result = initial;
        for (T value : values) {
            result = function.apply(result, value);
        }
        return result;
    }
}
