package org.zwobble.shed.compiler;

import java.util.List;

import org.zwobble.shed.compiler.errors.CompilerError;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.ParseResult;
import org.zwobble.shed.compiler.parsing.Parser;
import org.zwobble.shed.compiler.parsing.TokenNavigator;
import org.zwobble.shed.compiler.parsing.nodes.EntireSourceNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.tokeniser.TokenisedSource;
import org.zwobble.shed.compiler.typechecker.SimpleNodeLocations;
import org.zwobble.shed.compiler.util.Eager;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

public class ParserStage implements CompilerStage {
    private final Parser parser;

    public ParserStage() {
        this.parser = new Parser();
    }
    
    @Override
    public CompilerStageResult execute(CompilationData data) {
        Iterable<TokenisedSource> tokenisedSources = data.get(CompilationDataKeys.tokenisedSources);
        List<ParseResult<SourceNode>> parseResults = Eager.transform(tokenisedSources, parse());
        
        CompilerStageResult result = CompilerStageResult.create(errors(parseResults));
        result.add(CompilationDataKeys.nodeLocations, nodeLocations(parseResults));
        result.add(CompilationDataKeys.unorderedSourceNodes, sourceNodes(parseResults));
        return result;
    }

    private Function<TokenisedSource, ParseResult<SourceNode>> parse() {
        return new Function<TokenisedSource, ParseResult<SourceNode>>() {
            @Override
            public ParseResult<SourceNode> apply(TokenisedSource input) {
                return parser.parse(new TokenNavigator(input));
            }
        };
    }
    
    private List<CompilerError> errors(List<ParseResult<SourceNode>> parseResults) {
        return ImmutableList.copyOf(concat(transform(parseResults, toErrors())));
    }

    private Function<ParseResult<SourceNode>, Iterable<CompilerError>> toErrors() {
        return new Function<ParseResult<SourceNode>, Iterable<CompilerError>>() {
            @Override
            public Iterable<CompilerError> apply(ParseResult<SourceNode> input) {
                return input.getErrors();
            }
        };
    }

    private NodeLocations nodeLocations(List<ParseResult<SourceNode>> parseResults) {
        return SimpleNodeLocations.combine(parseResults);
    }

    private EntireSourceNode sourceNodes(List<ParseResult<SourceNode>> parseResults) {
        return new EntireSourceNode(Eager.transform(parseResults, getNode()));
    }
    private Function<ParseResult<SourceNode>, SourceNode> getNode() {
        return new Function<ParseResult<SourceNode>, SourceNode>() {
            @Override
            public SourceNode apply(ParseResult<SourceNode> input) {
                return input.get();
            }
        };
    }
}
