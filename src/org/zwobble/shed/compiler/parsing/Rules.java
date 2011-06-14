package org.zwobble.shed.compiler.parsing;

import java.util.ArrayList;
import java.util.List;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.Separator.Type;
import org.zwobble.shed.compiler.tokeniser.Keyword;
import org.zwobble.shed.compiler.tokeniser.Token;
import org.zwobble.shed.compiler.tokeniser.TokenPosition;
import org.zwobble.shed.compiler.tokeniser.TokenType;

import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.parsing.Result.success;
import static org.zwobble.shed.compiler.tokeniser.TokenType.WHITESPACE;

import static java.lang.String.format;
import static java.util.Arrays.asList;

public class Rules {
    public static <T, U> Rule<T> then(final Rule<U> originalRule, final ParseAction<U, T> action) {
        return new Rule<T>() {
            @Override
            public Result<T> parse(TokenIterator tokens) {
                Result<U> result = originalRule.parse(tokens);
                if (!result.hasValue()) {
                    return result.changeValue(null);
                }
                Result<T> actionResult = action.apply(result.get());
                if (actionResult.isSuccess()) {
                    return result.changeValue(actionResult.get());
                } else {
                    return result.changeValue(null);
                }
            }
        };
    }
    
    public static Rule<RuleValues> sequence(final OnError recovery, final Rule<?>... rules) {
        return new Rule<RuleValues>() {
            @Override
            public Result<RuleValues> parse(TokenIterator tokens) {
                TokenIterator startPosition = tokens.currentPosition();
                RuleValues values = new RuleValues();
                List<CompilerError> errors = new ArrayList<CompilerError>();
                
                for (Rule<?> rule : rules) {
                    Result<?> result = rule.parse(tokens);
                    if (result.anyErrors()) {
                        if (rule instanceof GuardRule && result.noMatch() && errors.isEmpty()) {
                            tokens.resetPosition(startPosition);
                            return result.toType(null, Result.Type.NO_MATCH);                            
                        }

                        errors.addAll(result.getErrors());
                        if (!result.ruleDidFinish() || recovery == OnError.FINISH) {
                            return new Result<RuleValues>(null, errors, Result.Type.FATAL);                                
                        }
                    }
                    values.add(rule, result.get());
                }
                if (errors.isEmpty()) {
                    return success(values);                    
                } else {
                    return new Result<RuleValues>(values, errors, Result.Type.ERROR_RECOVERED_WITH_VALUE);
                }
                
            }
        };
    }
    
    public static <T> Rule<List<T>> oneOrMoreWithSeparator(final Rule<T> rule, final Separator<?> separator) {
        return repeatedWithSeparator(rule, separator, false);
    }
    
    public static <T> Rule<List<T>> zeroOrMoreWithSeparator(final Rule<T> rule, final Separator<?> separator) {
        return repeatedWithSeparator(rule, separator, true);
    }
    
    private static <T> Rule<List<T>> repeatedWithSeparator(final Rule<T> rule, final Separator<?> separator, final boolean allowEmpty) {
        return new Rule<List<T>>() {
            @Override
            public Result<List<T>> parse(TokenIterator tokens) {
                List<T> values = new ArrayList<T>();
                List<CompilerError> errors = new ArrayList<CompilerError>();
                Result<T> firstResult = rule.parse(tokens);
                if (firstResult.anyErrors()) {
                    if (allowEmpty && firstResult.noMatch()) {
                        return success(values);
                    }
                    if (!firstResult.ruleDidFinish()) {
                        return firstResult.changeValue(null);
                    }
                    errors.addAll(firstResult.getErrors());
                }
                if (firstResult.hasValue()) {
                    values.add(firstResult.get());                    
                }
                while (true) {
                    TokenIterator positionBeforeSeparator = tokens.currentPosition();
                    Result<?> separatorResult = separator.parse(tokens);
                    if (separatorResult.anyErrors()) {
                        if (separatorResult.noMatch()) {
                            if (errors.isEmpty()) {
                                return success(values);                    
                            } else {
                                return new Result<List<T>>(values, errors, Result.Type.ERROR_RECOVERED);
                            }
                        }
                        return firstResult.changeValue(values);
                    }
                    
                    Result<T> ruleResult = rule.parse(tokens);
                    if (ruleResult.anyErrors()) {
                        if (separator.getType() == Type.SOFT && ruleResult.noMatch()) {
                            tokens.resetPosition(positionBeforeSeparator);
                            if (errors.isEmpty()) {
                                return success(values);                    
                            } else {
                                return new Result<List<T>>(values, errors, Result.Type.ERROR_RECOVERED);
                            }
                        }
                        if (!firstResult.ruleDidFinish()) {
                            return ruleResult.changeValue(values);                            
                        }
                        errors.addAll(ruleResult.getErrors());
                    }
                    if (ruleResult.hasValue()) {
                        values.add(ruleResult.get());                    
                    }
                }
            }
        };
    }
    
    public static <T> Rule<Option<T>> optional(final Rule<T> rule) {
        return new Rule<Option<T>>() {
            @Override
            public Result<Option<T>> parse(TokenIterator tokens) {
                Result<T> result = rule.parse(tokens);
                if (result.anyErrors()) {
                    if (result.noMatch()) {
                        return success(Option.<T>none());                        
                    } else {
                        return result.changeValue(null);
                    }
                }
                return success(some(result.get()));
            }
        };
    }
    
    public static <T> Rule<T> guard(Rule<T> rule) {
        return new GuardRule<T>(rule);
    }
    
    public static Rule<Keyword> keyword(final Keyword keyword) {
        return then(
            token(Token.keyword(keyword)),
            new ParseAction<Void, Keyword>() {
                @Override
                public Result<Keyword> apply(Void result) {
                    return success(keyword);
                }
            }
        ); 
        
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
            public Result<String> parse(TokenIterator tokens) {
                TokenPosition firstToken = tokens.peek();
                if (firstToken.getToken().getType() != type) {
                    return error(tokens, type, Result.Type.NO_MATCH);
                }
                return success(tokens.next().getToken().getValue());
            }
        };
    }
    
    public static Rule<Void> token(final Token expectedToken) {
        return new Rule<Void>() {
            @Override
            public Result<Void> parse(TokenIterator tokens) {
                TokenPosition firstToken = tokens.peek();
                if (!firstToken.getToken().equals(expectedToken)) {
                    return error(tokens, expectedToken, Result.Type.NO_MATCH);
                }
                tokens.next();
                return success(null);
            }
        };
    }

    public static <T> Rule<T> firstOf(final String name, final Rule<? extends T>... rules) {
        return new Rule<T>() {
            @SuppressWarnings("unchecked")
            @Override
            public Result<T> parse(TokenIterator tokens) {
                for (Rule<? extends T> rule : rules) {
                    Result<? extends T> result = rule.parse(tokens);
                    if (!result.noMatch()) {
                        return (Result<T>) result;
                    }
                }
                return error(tokens, name, Result.Type.NO_MATCH);
            }
        };
    }
    
    private static <T> Result<T> error(TokenIterator startOfError, TokenType tokenType, Result.Type type) {
        return error(startOfError, tokenType.name().toLowerCase(), type);
    }
    
    private static <T> Result<T> error(TokenIterator startOfError, Token token, Result.Type type) {
        return error(startOfError, token.describe(), type);
    }
    
    private static <T> Result<T> error(TokenIterator startOfError, Object expected, Result.Type type) {
        TokenPosition actual = startOfError.peek();
        TokenPosition endOfError = startOfError.hasNext(1) ? startOfError.peek(1) : actual;
        Token actualToken = actual.getToken();
        String message = format("Expected %s but got %s", expected, actualToken.describe());
        return new Result<T>(
            null,
            asList(new CompilerError(
                actual.getPosition(), endOfError.getPosition(), message
            )),
            type
        );
    }
}