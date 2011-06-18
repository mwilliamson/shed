package org.zwobble.shed.compiler.parsing;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.zwobble.shed.compiler.HasErrors;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNodeIdentifier;

import com.google.common.collect.ImmutableMap;

import static java.util.Arrays.asList;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Result<T> implements HasErrors {
    public static Iterable<Result<?>> subResults(Result<?>... results) {
        return asList(results);
    }
    
    public static <T> Result<T> success(T value, Iterable<? extends Result<?>> subResults) {
        return new Result<T>(value, Collections.<CompilerError>emptyList(), Type.SUCCESS, resultsToNodePositions(subResults));
    }
    
    public static <T> Result<T> fatal(List<CompilerError> errors, Iterable<? extends Result<?>> subResults) {
        return new Result<T>(null, errors, Type.FATAL, resultsToNodePositions(subResults));
    }
    
    public static <T> Result<T> errorRecoveredWithValue(T value, List<CompilerError> errors, Iterable<? extends Result<?>> subResults) {
        return new Result<T>(value, errors, Type.ERROR_RECOVERED_WITH_VALUE, resultsToNodePositions(subResults));
    }
    
    public static <T> Result<T> errorRecovered(List<CompilerError> errors, Iterable<? extends Result<?>> subResults) {
        return new Result<T>(null, errors, Type.ERROR_RECOVERED, resultsToNodePositions(subResults));
    }
    
    public static <T> Result<T> result(T value, List<CompilerError> errors, Type type, Iterable<? extends Result<?>> subResults) {
        return new Result<T>(value, errors, type, resultsToNodePositions(subResults));
    }
    
    private static Map<SyntaxNodeIdentifier, SourceRange> resultsToNodePositions(Iterable<? extends Result<?>> subResults) {
        ImmutableMap.Builder<SyntaxNodeIdentifier, SourceRange> nodePositions = ImmutableMap.builder();
        for (Result<?> subResult : subResults) {
            nodePositions.putAll(subResult.nodePositions);            
        }
        return nodePositions.build();
    }
    
    private final T value;
    private final List<CompilerError> errors;
    private final Type type;
    private final Map<SyntaxNodeIdentifier, SourceRange> nodePositions;
    
    public boolean anyErrors() {
        return !errors.isEmpty();
    }
    
    public <U> Result<U> changeValue(U value) {
        return new Result<U>(value, errors, type, nodePositions);
    }
    
    public <U> Result<U> toType(U value, Type type) {
        return new Result<U>(value, errors, type, nodePositions);
    }
    
    public T get() {
        return value;
    }
    
    public SourceRange positionOf(SyntaxNode node) {
        return nodePositions.get(new SyntaxNodeIdentifier(node));
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
}
