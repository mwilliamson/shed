package org.zwobble.shed.compiler.files;

import java.io.File;
import java.net.URISyntaxException;

import org.zwobble.shed.compiler.ShedCompiler;

import com.google.common.base.Function;

import static com.google.common.collect.Iterables.transform;
import static org.zwobble.shed.compiler.files.ShedFiles.listFilesRecursively;

public class ResourceRuntimeFileReader implements RuntimeFileReader {
    public static ResourceRuntimeFileReader build() {
        return new ResourceRuntimeFileReader("/org/zwobble/shed/runtime/");
    }
    
    private final String resourceRoot;

    private ResourceRuntimeFileReader(String resourceRoot) {
        this.resourceRoot = resourceRoot;
    }
    
    @Override
    public Iterable<RuntimeFile> listFiles() {
        return transform(listFilesRecursively(findFile(".")), toRuntimeFile());
    }

    @Override
    public RuntimeFile find(String path) {
        return new RuntimeFile(findFile(path));
    }
    
    private File findFile(String path) {
        try {
            return new File(ShedCompiler.class.getResource(resourceRoot + path).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    
    private Function<File, RuntimeFile> toRuntimeFile() {
        return new Function<File, RuntimeFile>() {
            @Override
            public RuntimeFile apply(File input) {
                return new RuntimeFile(input);
            }
        };
    }
}
