package org.zwobble.shed.compiler.files;

import java.io.File;

import com.google.common.base.Function;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

public class ShedFiles {
    public static Iterable<File> listFilesRecursively(File file) {
        if (file.isFile()) {
            return singleton(file);
        } else if (file.isDirectory()) {
            return concat(transform(asList(file.listFiles()), toRecursiveFileList()));
        } else {
            throw new RuntimeException("file is neither a file nor a directory");
        }
    }

    private static Function<File, Iterable<File>> toRecursiveFileList() {
        return new Function<File, Iterable<File>>() {
            @Override
            public Iterable<File> apply(File input) {
                return listFilesRecursively(input);
            }
        };
    }
}
