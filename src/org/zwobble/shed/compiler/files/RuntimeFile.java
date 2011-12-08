package org.zwobble.shed.compiler.files;


public interface RuntimeFile {
    boolean isFile();
    String readAll();
    String path();
}
