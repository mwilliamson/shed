package org.zwobble.shed.compiler.parsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.SimpleCompilerError;
import org.zwobble.shed.compiler.parsing.Separator.Type;
import org.zwobble.shed.compiler.parsing.nodes.Node;
import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.tokeniser.Keyword;
import org.zwobble.shed.compiler.tokeniser.Token;
import org.zwobble.shed.compiler.tokeniser.TokenPosition;
import org.zwobble.shed.compiler.tokeniser.TokenType;

import com.google.common.collect.ImmutableMap;

import static java.lang.String.format;
import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.parsing.ParseResult.errorRecoveredWithValue;
import static org.zwobble.shed.compiler.parsing.ParseResult.fatal;
import static org.zwobble.shed.compiler.parsing.ParseResult.subResults;
import static org.zwobble.shed.compiler.parsing.ParseResult.success;
import static org.zwobble.shed.compiler.parsing.Separator.softSeparator;

public class Rules {
    public static <T, U> Rule<T> then(final Rule<U> originalRule, final SimpleParseAction<U, T> action) {
        return new Rule<T>() {
            @SuppressWarnings("unchecked")
            @Override
            public ParseResult<T> parse(TokenNavigator tokens) {
                SourcePosition start = tokens.currentPosition();
                ParseResult<U> result = originalRule.parse(tokens);
                if (!result.hasValue()) {
                    return result.changeValue(null);
                }
                T actionResult = action.apply(result.get());
                SourcePosition end = tokens.lastPosition();
                if (actionResult instanceof Node) {
                    return (ParseResult<T>)result.changeValue((Node)actionResult, new SourceRange(start, end));
                } else {
                    return result.changeValue(actionResult);
                }
            }
        };
    }
    
    public static <T, U> Rule<T> then(final Rule<U> originalRule, final ParseAction<U, T> action) {
        return new Rule<T>() {
            @Override
            public ParseResult<T> parse(TokenNavigator tokens) {
                ParseResult<U> result = originalRule.parse(tokens);
                if (!result.hasValue()) {
                    return result.changeValue(null);
                }
                return action.apply(result);
            }
        };
    }
    
    public static Rule<RuleValues> sequence(final OnError recovery, final Rule<?>... rules) {
        return new Rule<RuleValues>() {
            @Override
            public ParseResult<RuleValues> parse(TokenNavigator tokens) {
                TokenNavigator startPosition = tokens.currentState();
                RuleValues values = new RuleValues();
                List<CompilerError> errors = new ArrayList<CompilerError>();
                List<ParseResult<?>> subResults = new ArrayList<ParseResult<?>>();
                
                for (Rule<?> rule : rules) {
                    ParseResult<?> result = rule.parse(tokens);
                    subResults.add(result);
                    if (result.anyErrors()) {
                        if (rule instanceof GuardRule && result.noMatch() && errors.isEmpty()) {
                            tokens.reset(startPosition);
                            return result.toType(null, ParseResult.Type.NO_MATCH);                            
                        }

                        errors.addAll(result.getErrors());
                        if (!result.ruleDidFinish() || recovery == OnError.FINISH) {
                            return fatal(errors, subResults);                                
                        }
                    }
                    values.add(rule, result.get());
                }
                if (errors.isEmpty()) {
                    return success(values, subResults);                    
                } else {
                    return errorRecoveredWithValue(values, errors, subResults);
                }
                
            }
        };
    }
    
    public static <T> Rule<List<T>> oneOrMoreWithSeparator(final Rule<T> rule, final Separator<?> separator) {
        return repeatedWithSeparator(rule, separator, false);
    }
    
    public static <T> Rule<List<T>> oneOrMore(final Rule<T> rule) {
        return oneOrMoreWithSeparator(rule, softSeparator(emptyRule()));
    }
    
    public static <T> Rule<List<T>> zeroOrMoreWithSeparator(final Rule<T> rule, final Separator<?> separator) {
        return repeatedWithSeparator(rule, separator, true);
    }
    
    public static <T> Rule<List<T>> zeroOrMore(final Rule<T> rule) {
        return zeroOrMoreWithSeparator(rule, softSeparator(emptyRule()));
    }
    
    private static Rule<Void> emptyRule() {
        return new Rule<Void>() {
            @Override
            public ParseResult<Void> parse(TokenNavigator tokens) {
                return ParseResult.success(null, Collections.<ParseResult<?>>emptyList());
            }
        };
    }

    private static <T> Rule<List<T>> repeatedWithSeparator(final Rule<T> rule, final Separator<?> separator, final boolean allowEmpty) {
        return new Rule<List<T>>() {
            @Override
            public ParseResult<List<T>> parse(TokenNavigator tokens) {
                List<T> values = new ArrayList<T>();
                List<ParseResult<?>> subResults = new ArrayList<ParseResult<?>>();
                List<CompilerError> errors = new ArrayList<CompilerError>();
                
                ParseResult<T> firstResult = rule.parse(tokens);
                subResults.add(firstResult);
                if (firstResult.anyErrors()) {
                    if (allowEmpty && firstResult.noMatch()) {
                        return success(values, subResults);
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
                    TokenNavigator positionBeforeSeparator = tokens.currentState();
                    ParseResult<?> separatorResult = separator.parse(tokens);
                    subResults.add(separatorResult);
                    if (separatorResult.anyErrors()) {
                        if (separatorResult.noMatch()) {
                            if (errors.isEmpty()) {
                                return success(values, subResults);                    
                            } else {
                                return errorRecoveredWithValue(values, errors, subResults);
                            }
                        }
                        return firstResult.changeValue(values);
                    }
                    
                    ParseResult<T> ruleResult = rule.parse(tokens);
                    subResults.add(ruleResult);
                    if (ruleResult.anyErrors()) {
                        if (separator.getType() == Type.SOFT && ruleResult.noMatch()) {
                            tokens.reset(positionBeforeSeparator);
                            if (errors.isEmpty()) {
                                return success(values, subResults);                    
                            } else {
                                return errorRecoveredWithValue(values, errors, subResults);
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
            public ParseResult<Option<T>> parse(TokenNavigator tokens) {
                ParseResult<T> result = rule.parse(tokens);
                if (result.anyErrors()) {
                    if (result.noMatch()) {
                        return success(Option.<T>none(), Collections.<ParseResult<?>>emptyList());                        
                    } else {
                        return result.changeValue(null);
                    }
                }
                return success(some(result.get()), subResults(result));
            }
        };
    }
    
    public static <T> Rule<T> guard(Rule<T> rule) {
        return new GuardRule<T>(rule);
    }
    
    public static Rule<Keyword> keyword(final Keyword keyword) {
        return then(
            token(Token.keyword(keyword)),
            new SimpleParseAction<Void, Keyword>() {
                @Override
                public Keyword apply(Void result) {
                    return keyword;
                }
            }
        ); 
        
    }
    
    public static Rule<Void> symbol(String symbol) {
        return token(Token.symbol(symbol));
    }
    
    public static Rule<String> tokenOfType(final TokenType type) {
        return new Rule<String>() {
            @Override
            public ParseResult<String> parse(TokenNavigator tokens) {
                TokenPosition firstToken = tokens.peek();
                if (firstToken.getToken().getType() != type) {
                    return error(tokens, type, ParseResult.Type.NO_MATCH);
                }
                return success(tokens.next().getToken().getValue(), Collections.<ParseResult<?>>emptyList());
            }
        };
    }
    
    public static Rule<Void> token(final Token expectedToken) {
        return new Rule<Void>() {
            @Override
            public ParseResult<Void> parse(TokenNavigator tokens) {
                TokenPosition firstToken = tokens.peek();
                if (!firstToken.getToken().equals(expectedToken)) {
                    return error(tokens, expectedToken, ParseResult.Type.NO_MATCH);
                }
                tokens.next();
                return success(null, Collections.<ParseResult<?>>emptyList());
            }
        };
    }

    public static <T> Rule<T> firstOf(final String name, final Rule<? extends T>... rules) {
        return new Rule<T>() {
            @SuppressWarnings("unchecked")
            @Override
            public ParseResult<T> parse(TokenNavigator tokens) {
                for (Rule<? extends T> rule : rules) {
                    ParseResult<? extends T> result = rule.parse(tokens);
                    if (!result.noMatch()) {
                        return (ParseResult<T>) result;
                    }
                }
                return error(tokens, name, ParseResult.Type.NO_MATCH);
            }
        };
    }
    
    private static <T> ParseResult<T> error(TokenNavigator startOfError, TokenType tokenType, ParseResult.Type type) {
        return error(startOfError, tokenType.name().toLowerCase(), type);
    }
    
    private static <T> ParseResult<T> error(TokenNavigator startOfError, Token token, ParseResult.Type type) {
        return error(startOfError, token.describe(), type);
    }
    
    private static <T> ParseResult<T> error(TokenNavigator startOfError, Object expected, ParseResult.Type type) {
        TokenPosition tokenPosition = startOfError.peek();
        String message = format("Expected %s but got %s", expected, tokenPosition.getToken().describe());
        return new ParseResult<T>(
            null,
            Arrays.<CompilerError>asList(new SimpleCompilerError(tokenPosition.getSourceRange(), message)),
            type,
            ImmutableMap.<Identity<?>, SourceRange>of()
        );
    }
}
