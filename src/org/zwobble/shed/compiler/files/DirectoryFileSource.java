package org.zwobble.shed.compiler.files;

import java.io.File;
import java.util.Iterator;

import static org.zwobble.shed.compiler.files.ShedFiles.toRuntimeFile;

import static com.google.common.collect.Iterables.transform;
import static org.zwobble.shed.compiler.files.ShedFiles.listFilesRecursively;

public class DirectoryFileSource implements FileSource {
    public static DirectoryFileSource create(File directory) {
        return new DirectoryFileSource(directory);
    }
    
    public static DirectoryFileSource create(String directory) {
        return create(new File(directory));
    }
    
    private final File directory;

    private DirectoryFileSource(File directory) {
        this.directory = directory;
    }
    
    @Override
    public Iterator<RuntimeFile> iterator() {
        return transform(listFilesRecursively(directory), toRuntimeFile()).iterator();
    }
}
