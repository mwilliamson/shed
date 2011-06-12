package org.zwobble.shed.parser.parsing;

import java.util.List;

import org.zwobble.shed.parser.tokeniser.Tokeniser;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class ParserTesting {
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

    public static TokenIterator tokens(String input) {
        return new TokenIterator(new Tokeniser().tokenise(input));
    }
}
