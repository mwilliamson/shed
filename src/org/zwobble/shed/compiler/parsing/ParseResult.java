package org.zwobble.shed.compiler.parsing;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.zwobble.shed.compiler.errors.CompilerError;
import org.zwobble.shed.compiler.errors.HasErrors;
import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.Node;
import org.zwobble.shed.compiler.util.Pair;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.util.Pair.pair;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ParseResult<T> implements HasErrors, NodeLocations {
    public static Iterable<ParseResult<?>> subResults(ParseResult<?>... results) {
        return asList(results);
    }
    
    public static <T> ParseResult<T> success(T value, Iterable<? extends ParseResult<?>> subResults) {
        return new ParseResult<T>(value, Collections.<CompilerError>emptyList(), Type.SUCCESS, resultsToNodePositions(subResults));
    }
    
    public static <T> ParseResult<T> fatal(List<CompilerError> errors, Iterable<? extends ParseResult<?>> subResults) {
        return new ParseResult<T>(null, errors, Type.FATAL, resultsToNodePositions(subResults));
    }
    
    public static <T> ParseResult<T> errorRecoveredWithValue(T value, List<CompilerError> errors, Iterable<? extends ParseResult<?>> subResults) {
        return new ParseResult<T>(value, errors, Type.ERROR_RECOVERED_WITH_VALUE, resultsToNodePositions(subResults));
    }
    
    public static <T> ParseResult<T> errorRecovered(List<CompilerError> errors, Iterable<? extends ParseResult<?>> subResults) {
        return new ParseResult<T>(null, errors, Type.ERROR_RECOVERED, resultsToNodePositions(subResults));
    }
    
    public static <T> ParseResult<T> result(T value, List<CompilerError> errors, Type type, Iterable<? extends ParseResult<?>> subResults) {
        return new ParseResult<T>(value, errors, type, resultsToNodePositions(subResults));
    }
    
    private static Map<Identity<Node>, SourceRange> resultsToNodePositions(Iterable<? extends ParseResult<?>> subResults) {
        ImmutableMap.Builder<Identity<Node>, SourceRange> nodePositions = ImmutableMap.builder();
        for (ParseResult<?> subResult : subResults) {
            nodePositions.putAll(subResult.nodePositions);            
        }
        return nodePositions.build();
    }
    
    private final T value;
    private final List<CompilerError> errors;
    private final Type type;
    private final Map<Identity<Node>, SourceRange> nodePositions;
    
    public boolean anyErrors() {
        return !errors.isEmpty();
    }
    
    public <U> ParseResult<U> changeValue(U value) {
        return new ParseResult<U>(value, errors, type, nodePositions);
    }
    
    public <U extends Node> ParseResult<U> changeValue(U value, SourceRange position) {
        Map<Identity<Node>, SourceRange> newPositions = new HashMap<Identity<Node>, SourceRange>();
        newPositions.putAll(nodePositions);
        Identity<Node> nodeIdentifier = new Identity<Node>(value);
        if (newPositions.containsKey(nodeIdentifier)) {
            if (!position.contains(newPositions.get(nodeIdentifier))) {
                throw new RuntimeException("The same node cannot appear in two places");
            }
        } else {
            newPositions.put(nodeIdentifier, position);
        }
        
        return new ParseResult<U>(value, errors, type, newPositions);
    }
    
    public <U> ParseResult<U> toType(U value, Type type) {
        return new ParseResult<U>(value, errors, type, nodePositions);
    }
    
    public T get() {
        return value;
    }
    
    @Override
    public SourceRange locate(Node node) {
        return nodePositions.get(new Identity<Node>(node));
    }
    
    public List<CompilerError> getErrors() {
        return errors;
    }
    
    public static enum Type {
        SUCCESS,
        NO_MATCH,
        ERROR_RECOVERED,
        ERROR_RECOVERED_WITH_VALUE,
        FATAL
    }
    
    public boolean isSuccess() {
        return type == Type.SUCCESS;
    }
    
    public boolean hasValue() {
        return type == Type.SUCCESS || type == Type.ERROR_RECOVERED_WITH_VALUE;
    }

    public boolean ruleDidFinish() {
        return type == Type.SUCCESS || type == Type.ERROR_RECOVERED || type == Type.ERROR_RECOVERED_WITH_VALUE;
    }
    public boolean noMatch() {
        return type == Type.NO_MATCH;
    }
    public boolean isFatal() {
        return type == Type.FATAL;
    }
    
    @Override
    public Iterable<Pair<Node, SourceRange>> all() {
        return transform(nodePositions.entrySet(), toPair());
    }
    
    private Function<Map.Entry<Identity<Node>, SourceRange>, Pair<Node, SourceRange>> toPair() {
        return new Function<Map.Entry<Identity<Node>,SourceRange>, Pair<Node,SourceRange>>() {
            @Override
            public Pair<Node, SourceRange> apply(Map.Entry<Identity<Node>, SourceRange> input) {
                return pair(input.getKey().get(), input.getValue());
            }
        };
    }
}
