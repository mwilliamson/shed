package org.zwobble.shed.parser.parsing;

import org.zwobble.shed.parser.tokeniser.Token;
import org.zwobble.shed.parser.tokeniser.TokenType;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

public class Parser {
    public SourceNode source(Iterable<Token> tokens) {
        return new SourceNode(parsePackageDeclaration(tokens));
    }
    
    public PackageDeclarationNode parsePackageDeclaration(Iterable<Token> tokens) {
        return new PackageDeclarationNode(copyOf(transform(filter(tokens, isIdentifier()), toValue())));
    }

    private Predicate<Token> isIdentifier() {
        return new Predicate<Token>() {
            @Override
            public boolean apply(Token input) {
                return input.getType() == TokenType.IDENTIFIER;
            }
        };
    }

    private Function<Token, String> toValue() {
        return new Function<Token, String>() {
            @Override
            public String apply(Token input) {
                return input.getValue();
            }
        };
    }
}
