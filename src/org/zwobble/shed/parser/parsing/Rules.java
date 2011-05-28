package org.zwobble.shed.parser.parsing;

import java.util.ArrayList;
import java.util.List;

import org.zwobble.shed.parser.tokeniser.Keyword;
import org.zwobble.shed.parser.tokeniser.Token;
import org.zwobble.shed.parser.tokeniser.TokenPosition;
import org.zwobble.shed.parser.tokeniser.TokenType;

import com.google.common.collect.PeekingIterator;

import static java.lang.String.format;
import static org.zwobble.shed.parser.parsing.Result.failure;

import static org.zwobble.shed.parser.parsing.Result.success;
import static org.zwobble.shed.parser.tokeniser.TokenType.WHITESPACE;

public class Rules {
    public static <T, U> Rule<T> then(final Rule<U> originalRule, final ParseAction<U, T> action) {
        return new Rule<T>() {
            @Override
            public Result<T> parse(PeekingIterator<TokenPosition> tokens) {
                Result<U> result = originalRule.parse(tokens);
                if (result.anyErrors()) {
                    return result.changeValue(null);
                }
                return action.apply(result.get());
            }
        };
    }
    
    public static Rule<RuleValues> sequence(final Rule<?>... rules) {
        return new Rule<RuleValues>() {
            @Override
            public Result<RuleValues> parse(PeekingIterator<TokenPosition> tokens) {
                RuleValues values = new RuleValues();
                for (Rule<?> rule : rules) {
                    Result<?> result = rule.parse(tokens);
                    if (result.anyErrors()) {
                        return result.changeValue(null);
                    }
                    values.add(rule, result.get());
                }
                return success(values);
            }
        };
    }
    
    public static <T> Rule<List<T>> oneOrMoreWithSeparator(final Rule<T> rule, final Rule<?> separator) {
        return repeatedWithSeparator(rule, separator, false);
    }
    
    public static <T> Rule<List<T>> zeroOrMoreWithSeparator(final Rule<T> rule, final Rule<?> separator) {
        return repeatedWithSeparator(rule, separator, true);
    }
    
    private static <T> Rule<List<T>> repeatedWithSeparator(final Rule<T> rule, final Rule<?> separator, final boolean allowEmpty) {
        return new Rule<List<T>>() {
            @Override
            public Result<List<T>> parse(PeekingIterator<TokenPosition> tokens) {
                List<T> values = new ArrayList<T>();
                Result<T> firstResult = rule.parse(tokens);
                if (firstResult.anyErrors()) {
                    if (allowEmpty) {
                        return success(values);
                    }
                    return firstResult.changeValue(null);
                }
                values.add(firstResult.get());
                while (true) {
                    Result<?> separatorResult = separator.parse(tokens);
                    if (separatorResult.anyErrors()) {
                        return success(values);
                    }
                    
                    Result<T> ruleResult = rule.parse(tokens);
                    if (ruleResult.anyErrors()) {
                        return ruleResult.changeValue(null);
                    }
                    values.add(ruleResult.get());
                }
            }
        };
    }
    
    public static <T> Rule<T> optional(final Rule<T> rule) {
        return new Rule<T>() {
            @Override
            public Result<T> parse(PeekingIterator<TokenPosition> tokens) {
                Result<T> result = rule.parse(tokens);
                if (result.anyErrors()) {
                    return success(null);
                }
                return result;
            }
        };
    }
    
    public static Rule<Void> keyword(final Keyword keyword) {
        return token(Token.keyword(keyword));
    }
    
    public static Rule<Void> symbol(String symbol) {
        return token(Token.symbol(symbol));
    }
    
    public static Rule<String> whitespace() {
        return tokenOfType(WHITESPACE);
    }
    
    public static Rule<String> tokenOfType(final TokenType type) {
        return new Rule<String>() {
            @Override
            public Result<String> parse(PeekingIterator<TokenPosition> tokens) {
                TokenPosition firstToken = tokens.peek();
                if (firstToken.getToken().getType() != type) {
                    return error(firstToken, type);
                }
                return success(tokens.next().getToken().getValue());
            }
        };
    }
    
    public static Rule<Void> token(final Token expectedToken) {
        return new Rule<Void>() {
            @Override
            public Result<Void> parse(PeekingIterator<TokenPosition> tokens) {
                TokenPosition firstToken = tokens.peek();
                if (!firstToken.getToken().equals(expectedToken)) {
                    return error(firstToken, expectedToken);
                }
                tokens.next();
                return success(null);
            }
        };
    }
    
    private static <T> Result<T> error(TokenPosition actual, TokenType tokenType) {
        return error(actual, tokenType.name().toLowerCase());
    }
    
    private static <T> Result<T> error(TokenPosition actual, Token token) {
        return error(actual, token.describe());
    }
    
    private static <T> Result<T> error(TokenPosition actual, Object expected) {
        return failure(new Error(actual.getLineNumber(), actual.getCharacterNumber(), format("Expected %s but got %s", expected, actual.getToken().describe())));
    }
}
