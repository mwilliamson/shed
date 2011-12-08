package org.zwobble.shed.compiler.files;

import java.util.Iterator;

import lombok.RequiredArgsConstructor;

import static com.google.common.collect.Iterators.singletonIterator;

@RequiredArgsConstructor(staticName="create")
public class StringFileSource implements FileSource {
    private final String path;
    private final String contents;
    
    @Override
    public Iterator<RuntimeFile> iterator() {
        RuntimeFile file = StringRuntimeFile.create(path, contents);
        return singletonIterator(file);
    }
}
