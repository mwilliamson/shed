package org.zwobble.shed.compiler.files;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName="create")
public class StringRuntimeFile implements RuntimeFile {
    private final String path;
    private final String contents;
    
    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public String readAll() {
        return contents;
    }

    @Override
    public String path() {
        return path;
    }
}
