package org.zwobble.shed.parser.parsing;

import java.util.List;

import org.zwobble.shed.parser.parsing.nodes.ExpressionNode;
import org.zwobble.shed.parser.parsing.nodes.ImmutableVariableNode;
import org.zwobble.shed.parser.parsing.nodes.ImportNode;
import org.zwobble.shed.parser.parsing.nodes.PackageDeclarationNode;
import org.zwobble.shed.parser.parsing.nodes.SourceNode;

import static org.zwobble.shed.parser.parsing.Result.success;
import static org.zwobble.shed.parser.parsing.Rules.guard;
import static org.zwobble.shed.parser.parsing.Rules.keyword;
import static org.zwobble.shed.parser.parsing.Rules.last;
import static org.zwobble.shed.parser.parsing.Rules.oneOrMoreWithSeparator;
import static org.zwobble.shed.parser.parsing.Rules.optional;
import static org.zwobble.shed.parser.parsing.Rules.sequence;
import static org.zwobble.shed.parser.parsing.Rules.symbol;
import static org.zwobble.shed.parser.parsing.Rules.then;
import static org.zwobble.shed.parser.parsing.Rules.tokenOfType;
import static org.zwobble.shed.parser.parsing.Rules.whitespace;
import static org.zwobble.shed.parser.parsing.Rules.zeroOrMoreWithSeparator;
import static org.zwobble.shed.parser.tokeniser.Keyword.IMPORT;
import static org.zwobble.shed.parser.tokeniser.Keyword.PACKAGE;
import static org.zwobble.shed.parser.tokeniser.Keyword.VAL;
import static org.zwobble.shed.parser.tokeniser.TokenType.IDENTIFIER;

public class Parser {
    public Rule<SourceNode> source() {
        final Rule<PackageDeclarationNode> packageDeclaration;
        final Rule<List<ImportNode>> imports;
        return then(
            sequence(
                packageDeclaration = packageDeclaration(),
                optional(whitespace()),
                imports = zeroOrMoreWithSeparator(importNode(), whitespace())
            ),
            new ParseAction<RuleValues, SourceNode>() {
                @Override
                public Result<SourceNode> apply(RuleValues result) {
                    return success(new SourceNode(result.get(packageDeclaration), result.get(imports)));
                }
            }
        );
    }
    
    public Rule<PackageDeclarationNode> packageDeclaration() {
        final Rule<List<String>> names;
        return then(
            sequence(
                guard(keyword(PACKAGE)),
                whitespace(),
                names = dotSeparatedIdentifiers(),
                last(symbol(";"))
            ),
            new ParseAction<RuleValues, PackageDeclarationNode>() {
                @Override
                public Result<PackageDeclarationNode> apply(RuleValues result) {
                    return success(new PackageDeclarationNode(result.get(names)));
                }
            }
        );
    }

    public Rule<ImportNode> importNode() {
        final Rule<List<String>> names;
        return then(
            sequence(
                guard(keyword(IMPORT)),
                whitespace(),
                (names = dotSeparatedIdentifiers()),
                last(symbol(";"))
            ),
            new ParseAction<RuleValues, ImportNode>() {
                @Override
                public Result<ImportNode> apply(RuleValues result) {
                    return success(new ImportNode(result.get(names)));
                }
            }
        );
    }

    public Rule<ImmutableVariableNode> immutableVariable() {
        final Rule<String> identifier = tokenOfType(IDENTIFIER);
        final Rule<? extends ExpressionNode> expression = expression(); 
        return then(
            sequence(
                guard(keyword(VAL)), whitespace(),
                identifier, optional(whitespace()),
                symbol("="), optional(whitespace()),
                expression, optional(whitespace()),
                symbol(";")
            ),
            new ParseAction<RuleValues, ImmutableVariableNode>() {
                @Override
                public Result<ImmutableVariableNode> apply(RuleValues result) {
                    return success(new ImmutableVariableNode(result.get(identifier), result.get(expression)));
                }
            }
        );
    }
    
    public Rule<? extends ExpressionNode> expression() {
        return Literals.numberLiteral();
    }
    
    private Rule<List<String>> dotSeparatedIdentifiers() {
        return oneOrMoreWithSeparator(tokenOfType(IDENTIFIER), symbol("."));
    }
}
