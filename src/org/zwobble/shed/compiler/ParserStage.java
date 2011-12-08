package org.zwobble.shed.compiler;

import org.zwobble.shed.compiler.parsing.ParseResult;
import org.zwobble.shed.compiler.parsing.Parser;
import org.zwobble.shed.compiler.parsing.TokenNavigator;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;

public class ParserStage implements CompilerStage {
    private final Parser parser;

    public ParserStage() {
        this.parser = new Parser();
    }
    
    @Override
    public CompilerStageResult execute(CompilationData data) {
        ParseResult<SourceNode> parseResult = parser.parse(new TokenNavigator(data.get(CompilationDataKeys.tokenisedSource)));
        CompilerStageResult result = CompilerStageResult.create(parseResult.getErrors());
        result.add(CompilationDataKeys.nodeLocations, parseResult);
        result.add(CompilationDataKeys.sourceNode, parseResult.get());
        return result;
    }
}
