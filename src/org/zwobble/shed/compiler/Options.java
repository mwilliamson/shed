package org.zwobble.shed.compiler;

import java.util.ArrayList;
import java.util.List;

public class Options {
    public static <T> Option<List<T>> combine(Iterable<Option<? extends T>> options) {
        List<T> result = new ArrayList<T>();
        for (Option<? extends T> option : options) {
            if (!option.hasValue()) {
                return Option.none();
            }
            result.add(option.get());
        }
        return Option.some(result);
    }
}
