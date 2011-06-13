package org.zwobble.shed.compiler;

import java.util.List;

import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.Result;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class CompilerTesting {
    public static List<String> errorStrings(Result<?> result) {
        return Lists.transform(result.getErrors(), toErrorString());
    }
    
    private static Function<CompilerError, String> toErrorString() {
        return new Function<CompilerError, String>() {
            @Override
            public String apply(CompilerError input) {
                return input.getDescription();
            }
        };
    }

}
