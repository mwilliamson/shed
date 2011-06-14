package org.zwobble.shed.compiler;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IntRange implements Iterable<Integer> {
    public static IntRange range(int size) {
        return new IntRange(size);
    }
    
    private final int size;
    
    private IntRange(int size) {
        this.size = size;
    }
    
    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            private int position = 0;
            
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public Integer next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return position++;
            }
            
            @Override
            public boolean hasNext() {
                return position < size;
            }
        };
    }
}
