package org.zwobble.shed.compiler;

import com.google.common.collect.PeekingIterator;

public interface PsychicIterator<E> extends PeekingIterator<E> {
    E peek(int offset);
}
