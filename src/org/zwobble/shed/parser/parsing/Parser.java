package org.zwobble.shed.parser.parsing;

import java.util.ArrayList;
import java.util.List;

import org.zwobble.shed.parser.tokeniser.Keyword;
import org.zwobble.shed.parser.tokeniser.TokenPosition;
import org.zwobble.shed.parser.tokeniser.TokenType;

import com.google.common.collect.PeekingIterator;

import static java.lang.String.format;
import static org.zwobble.shed.parser.parsing.Result.failure;
import static org.zwobble.shed.parser.parsing.Result.success;
import static org.zwobble.shed.parser.tokeniser.Keyword.PACKAGE;
import static org.zwobble.shed.parser.tokeniser.Token.keyword;
import static org.zwobble.shed.parser.tokeniser.Token.symbol;
import static org.zwobble.shed.parser.tokeniser.TokenType.IDENTIFIER;
import static org.zwobble.shed.parser.tokeniser.TokenType.WHITESPACE;

public class Parser {
    public Result<SourceNode> source(PeekingIterator<TokenPosition> tokens) {
        Result<PackageDeclarationNode> packageDeclaration = parsePackageDeclaration(tokens);
        if (packageDeclaration.anyErrors()) {
            return packageDeclaration.changeValue(null);
        }
        return success(new SourceNode(packageDeclaration.get()));
    }
    
    public Result<PackageDeclarationNode> parsePackageDeclaration(PeekingIterator<TokenPosition> tokens) {
        Result<Void> keywordResult = consumeKeyword(tokens, PACKAGE);
        if (keywordResult.anyErrors()) {
            return keywordResult.changeValue(null);
        }
        
        Result<String> whitespaceResult = consumeWhitespace(tokens);
        if (whitespaceResult.anyErrors()) {
            return whitespaceResult.changeValue(null);
        }
        
        List<String> names = new ArrayList<String>();
        Result<String> identifierResult = consumeIdentifier(tokens);
        if (identifierResult.anyErrors()) {
            return identifierResult.changeValue(null);
        }
        names.add(identifierResult.get());
        while (true) {
            TokenPosition next = tokens.next();
            if (next.getToken().equals(symbol(";"))) {
                return success(new PackageDeclarationNode(names));
            }
            if (next.getToken().equals(symbol("."))) {
                identifierResult = consumeIdentifier(tokens);
                if (identifierResult.anyErrors()) {
                    return identifierResult.changeValue(null);
                }
                names.add(identifierResult.get());
            } else {
                return error(next, format("%s or %s", symbol("."), symbol(";")));
            }
        }
    }

    private Result<Void> consumeKeyword(PeekingIterator<TokenPosition> tokens, Keyword keyword) {
        TokenPosition firstToken = tokens.next();
        if (!firstToken.getToken().equals(keyword(keyword))) {
            return error(firstToken, keyword(keyword));
        }
        return success(null);
    }

    private Result<String> consumeWhitespace(PeekingIterator<TokenPosition> tokens) {
        return consumeTokenAndReturnValue(tokens, WHITESPACE);
    }
    
    private Result<String> consumeIdentifier(PeekingIterator<TokenPosition> tokens) {
        return consumeTokenAndReturnValue(tokens, IDENTIFIER);
    }
    
    private Result<String> consumeTokenAndReturnValue(PeekingIterator<TokenPosition> tokens, TokenType tokenType) {
        TokenPosition firstToken = tokens.next();
        if (firstToken.getToken().getType() != tokenType) {
            return error(firstToken, tokenType);
        }
        return success(firstToken.getToken().getValue());
    }
    
    private <T> Result<T> error(TokenPosition actual, Object expected) {
        return failure(new Error(actual.getLineNumber(), actual.getCharacterNumber(), format("Expected %s but got %s", expected, actual.getToken())));
    }
}
