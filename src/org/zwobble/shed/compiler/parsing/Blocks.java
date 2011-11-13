package org.zwobble.shed.compiler.parsing;

import java.util.List;

import org.zwobble.shed.compiler.parsing.nodes.BlockNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;

import static org.zwobble.shed.compiler.parsing.ParseResult.errorRecovered;
import static org.zwobble.shed.compiler.parsing.ParseResult.subResults;
import static org.zwobble.shed.compiler.parsing.Rules.guard;
import static org.zwobble.shed.compiler.parsing.Rules.sequence;
import static org.zwobble.shed.compiler.parsing.Rules.symbol;
import static org.zwobble.shed.compiler.parsing.Rules.then;
import static org.zwobble.shed.compiler.parsing.Rules.zeroOrMore;
import static org.zwobble.shed.compiler.parsing.Statements.statement;


public class Blocks {
    public static Rule<BlockNode> block() {
        return then(
            bracedStatementSequence(statement()),
            new SimpleParseAction<List<StatementNode>, BlockNode>() {
                @Override
                public BlockNode apply(List<StatementNode> result) {
                    return new BlockNode(result);
                }
            }
        );
    }
    
    public static <T> Rule<List<T>> bracedStatementSequence(Rule<T> statement) {
        final Rule<List<T>> statements = zeroOrMore(statement);
        return then(
            new Rule<RuleValues>() {
                @Override
                public ParseResult<RuleValues> parse(TokenNavigator tokens) {
                    Rule<RuleValues> sequence = sequence(OnError.FINISH,
                        guard(symbol("{")),
                        statements,
                        symbol("}")
                    );
                    ParseResult<RuleValues> result = sequence.parse(tokens);
                    if (!result.isFatal()) {
                        return result;
                    }
                    tokens.seekToEndOfBlock();
                    return errorRecovered(result.getErrors(), subResults(result));
                }
            },
            
            new SimpleParseAction<RuleValues, List<T>>() {
                @Override
                public List<T> apply(RuleValues result) {
                    return result.get(statements);
                }
            }
        );
        
    }
    
    public static Rule<List<StatementNode>> statements() {
        return zeroOrMore(statement());
    }
}
