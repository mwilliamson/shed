package org.zwobble.shed.compiler.files;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
            List<File> files = asList(file.listFiles());
            Collections.sort(files, directoriesFirst());
            return concat(transform(files, toRecursiveFileList()));
        } else {
            throw new RuntimeException("file is neither a file nor a directory");
        }
    }

    private static Comparator<File> directoriesFirst() {
        return new Comparator<File>() {
            @Override
            public int compare(File first, File second) {
                if (first.isDirectory() && !second.isDirectory()) {
                    return 1;
                }
                if (!first.isDirectory() && second.isDirectory()) {
                    return -1;
                }
                return first.getName().compareTo(second.getName());
            }
        };
    }

    private static Function<File, Iterable<File>> toRecursiveFileList() {
        return new Function<File, Iterable<File>>() {
            @Override
            public Iterable<File> apply(File input) {
                return listFilesRecursively(input);
            }
        };
    }
    
    public static Function<File, RuntimeFile> toRuntimeFile() {
        return new Function<File, RuntimeFile>() {
            @Override
            public RuntimeFile apply(File input) {
                return new RuntimeFile(input);
            }
        };
    }
}
