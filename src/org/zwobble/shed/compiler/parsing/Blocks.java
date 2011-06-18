package org.zwobble.shed.compiler.parsing;

import java.util.List;

import org.zwobble.shed.compiler.parsing.nodes.StatementNode;

import static org.zwobble.shed.compiler.parsing.Rules.guard;
import static org.zwobble.shed.compiler.parsing.Rules.optional;
import static org.zwobble.shed.compiler.parsing.Rules.sequence;
import static org.zwobble.shed.compiler.parsing.Rules.symbol;
import static org.zwobble.shed.compiler.parsing.Rules.then;
import static org.zwobble.shed.compiler.parsing.Rules.whitespace;
import static org.zwobble.shed.compiler.parsing.Rules.zeroOrMoreWithSeparator;
import static org.zwobble.shed.compiler.parsing.Separator.softSeparator;
import static org.zwobble.shed.compiler.parsing.Statements.statement;


public class Blocks {
    public static Rule<List<StatementNode>> block() {
        final Rule<List<StatementNode>> statements = statements();
        return then(
            new Rule<RuleValues>() {
                @Override
                public Result<RuleValues> parse(TokenIterator tokens) {
                    Rule<RuleValues> sequence = sequence(OnError.FINISH,
                        guard(symbol("{")),
                        optional(whitespace()),
                        statements,
                        optional(whitespace()),
                        symbol("}")
                    );
                    Result<RuleValues> result = sequence.parse(tokens);
                    if (!result.isFatal()) {
                        return result;
                    }
                    tokens.seekToEndOfBlock();
                    return new Result<RuleValues>(null, result.getErrors(), Result.Type.ERROR_RECOVERED);
                }
            },
            
            new ParseAction<RuleValues, List<StatementNode>>() {
                @Override
                public List<StatementNode> apply(RuleValues result) {
                    return result.get(statements);
                }
            }
        );
    }
    
    public static Rule<List<StatementNode>> statements() {
        return zeroOrMoreWithSeparator(statement(), softSeparator(whitespace()));
    }
}
