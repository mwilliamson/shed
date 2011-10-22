package org.zwobble.shed.compiler;

import java.util.Iterator;

import com.google.common.collect.UnmodifiableIterator;


public class ShedIterables {
    public static <T, R> R foldLeft(R initial, Iterable<T> values, Function2<R, T, R> function) {
        R result = initial;
        for (T value : values) {
            result = function.apply(result, value);
        }
        return result;
    }
    
    public static <T> T first(Iterable<T> iterable) {
        return iterable.iterator().next();
    }
    
    public static <F, T extends F> Iterable<T> safeCast(final Iterable<F> iterable, final Class<T> to) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new UnmodifiableIterator<T>() {
                    private final Iterator<F> iterator = iterable.iterator();
                    
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public T next() {
                        F next = iterator.next();
                        if (!to.isInstance(next)) {
                            throw new RuntimeException("Value: " + next + "\n is not instance of: " + to);
                        }
                        return (T)next;
                    }
                };
            }
        };
    }
}
