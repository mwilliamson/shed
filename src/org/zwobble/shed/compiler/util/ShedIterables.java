package org.zwobble.shed.compiler.util;

import java.util.Iterator;

import org.zwobble.shed.compiler.Option;


import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.UnmodifiableIterator;

import static org.zwobble.shed.compiler.Option.none;

import static org.zwobble.shed.compiler.Option.some;

import static org.zwobble.shed.compiler.util.Pair.pair;


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
    
    public static <T> Option<T> firstOrNone(Iterable<T> iterable) {
        Iterator<T> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            return some(iterator.next());
        } else {
            return none();
        }
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
    
    public static <T, U> Iterable<Pair<T, U>> zip(final Iterable<T> first, final Iterable<U> second) {
        return new Iterable<Pair<T,U>>() {
            @Override
            public Iterator<Pair<T, U>> iterator() {
                return new UnmodifiableIterator<Pair<T, U>>() {
                    private final Iterator<T> firstIterator = first.iterator();
                    private final Iterator<U> secondIterator = second.iterator();
                    
                    @Override
                    public boolean hasNext() {
                        return firstIterator.hasNext() && secondIterator.hasNext();
                    }

                    @Override
                    public Pair<T, U> next() {
                        return pair(firstIterator.next(), secondIterator.next());
                    }
                };
            }
        };
    }
    
    public static <T, U, V> Iterable<Triple<T, U, V>> zip(final Iterable<T> first, final Iterable<U> second, final Iterable<V> third) {
        return new Iterable<Triple<T,U, V>>() {
            @Override
            public Iterator<Triple<T, U, V>> iterator() {
                return new UnmodifiableIterator<Triple<T, U, V>>() {
                    private final Iterator<T> firstIterator = first.iterator();
                    private final Iterator<U> secondIterator = second.iterator();
                    private final Iterator<V> thirdIterator = third.iterator();
                    
                    @Override
                    public boolean hasNext() {
                        return firstIterator.hasNext() && secondIterator.hasNext() && thirdIterator.hasNext();
                    }

                    @Override
                    public Triple<T, U, V> next() {
                        return Triple.triple(firstIterator.next(), secondIterator.next(), thirdIterator.next());
                    }
                };
            }
        };
    }
    
    public static <T, U, V> Predicate<Triple<T, U, V>> unpack(final Predicate3<T, U, V> predicate) {
        return new Predicate<Triple<T,U,V>>() {
            @Override
            public boolean apply(Triple<T, U, V> input) {
                return predicate.apply(input.getFirst(), input.getSecond(), input.getThird());
            }
        };
    }
    
    public static <T, U, R> Function<Pair<T, U>, R> unpack(final Function2<T, U, R> function) {
        return new Function<Pair<T,U>, R>() {
            @Override
            public R apply(Pair<T, U> input) {
                return function.apply(input.getFirst(), input.getSecond());
            }
        };
    }
}
