package org.zwobble.shed.compiler.files;

import java.util.Iterator;

import static com.google.common.collect.Iterables.concat;
import static java.util.Arrays.asList;

public class DelegatingFileSource implements FileSource {
    private final Iterable<FileSource> fileSources;

    public static FileSource create(FileSource... fileSources) {
        return new DelegatingFileSource(asList(fileSources));
    }
    
    public static FileSource create(Iterable<FileSource> fileSources) {
        return new DelegatingFileSource(fileSources);
    }
    
    private DelegatingFileSource(Iterable<FileSource> fileSources) {
        this.fileSources = fileSources;
    }

    @Override
    public Iterator<RuntimeFile> iterator() {
        return concat(fileSources).iterator();
    }
}
