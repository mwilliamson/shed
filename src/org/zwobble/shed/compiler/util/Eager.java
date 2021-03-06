package org.zwobble.shed.compiler.util;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class Eager {
    public static <F, T> List<T> transform(Iterable<F> iterable, Function<? super F, ? extends T> function) {
        return Lists.newArrayList(Iterables.transform(iterable, function));
    }
}
