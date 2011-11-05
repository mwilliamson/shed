package org.zwobble.shed.compiler.files;

public interface RuntimeFileReader {
    Iterable<RuntimeFile> listFiles();
    RuntimeFile find(String path);
}
