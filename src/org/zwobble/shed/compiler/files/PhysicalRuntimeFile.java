package org.zwobble.shed.compiler.files;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class PhysicalRuntimeFile implements RuntimeFile {
    private final File file;

    public PhysicalRuntimeFile(File file) {
        this.file = file;
    }
    
    @Override
    public boolean isFile() {
        return file.isFile();
    }

    @Override
    public String readAll() {
        try {
            return Files.toString(file, Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String path() {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
