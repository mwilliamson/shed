package org.zwobble.shed.parser.parsing;

import org.zwobble.shed.parser.tokeniser.Keyword;
import org.zwobble.shed.parser.tokeniser.Token;
import org.zwobble.shed.parser.tokeniser.TokenType;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import static org.zwobble.shed.parser.tokeniser.Keyword.PACKAGE;

import static org.zwobble.shed.parser.tokeniser.Token.keyword;

import static java.lang.String.format;

import static org.zwobble.shed.parser.parsing.Result.failure;

import static org.zwobble.shed.parser.parsing.Result.success;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

public class Parser {
    public Result<SourceNode> source(Iterable<Token> tokens) {
        Result<PackageDeclarationNode> packageDeclaration = parsePackageDeclaration(tokens);
        if (packageDeclaration.anyErrors()) {
            return packageDeclaration.changeValue(null);
        }
        return success(new SourceNode(packageDeclaration.get()));
    }
    
    public Result<PackageDeclarationNode> parsePackageDeclaration(Iterable<Token> tokens) {
        Token firstToken = tokens.iterator().next();
        if (!firstToken.equals(keyword(PACKAGE))) {
            return failure(new Error(1, 1, format("Expected %s but got %s", keyword(PACKAGE), firstToken)));
        }
        return success(new PackageDeclarationNode(copyOf(transform(filter(tokens, isIdentifier()), toValue()))));
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
