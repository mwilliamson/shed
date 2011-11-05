package org.zwobble.shed.compiler.files;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class RuntimeFile {
    private final File file;

    public RuntimeFile(File file) {
        this.file = file;
    }
    
    public boolean isFile() {
        return file.isFile();
    }
    
    public String readAll() {
        try {
            return Files.toString(file, Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public String path() {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
