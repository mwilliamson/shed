package org.zwobble.shed.compiler;

import java.util.List;

public class PsychicIterators {
    public static <E> PsychicIterator<E> psychicIterator(List<E> list) {
        return new PsychicIteratorImpl<E>(list); 
    }
    
    private static class PsychicIteratorImpl<E> implements PsychicIterator<E> {
        private final List<E> list;
        private int nextIndex;
        
        private PsychicIteratorImpl(List<E> list) {
            this.list = list;
            this.nextIndex = 0;
        }
        
        @Override
        public boolean hasNext() {
            return nextIndex < list.size(); 
        }

        @Override
        public E peek() {
            return list.get(nextIndex);
        }
        
        public E peek(int offset) {
            return list.get(nextIndex + offset);
        }

        @Override
        public E next() {
            E next = list.get(nextIndex);
            nextIndex += 1;
            return next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
