package org.zwobble.shed.compiler.files;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Iterator;

import org.zwobble.shed.compiler.ShedCompiler;

import static com.google.common.collect.Iterables.transform;
import static org.zwobble.shed.compiler.files.ShedFiles.listFilesRecursively;
import static org.zwobble.shed.compiler.files.ShedFiles.toRuntimeFile;

public class ResourceFileSource implements FileSource {
    public static ResourceFileSource create() {
        return new ResourceFileSource("/org/zwobble/shed/runtime/");
    }
    
    private final String resourceRoot;

    private ResourceFileSource(String resourceRoot) {
        this.resourceRoot = resourceRoot;
    }
    
    @Override
    public Iterator<RuntimeFile> iterator() {
        return transform(listFilesRecursively(findFile(".")), toRuntimeFile()).iterator();
    }

    private File findFile(String path) {
        try {
            return new File(ShedCompiler.class.getResource(resourceRoot + path).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
