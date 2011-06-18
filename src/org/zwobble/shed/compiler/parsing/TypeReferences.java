package org.zwobble.shed.compiler.parsing;

import java.util.List;

import org.zwobble.shed.compiler.parsing.nodes.TypeApplicationNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeIdentifierNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeReferenceNode;

import static org.zwobble.shed.compiler.parsing.Rules.firstOf;
import static org.zwobble.shed.compiler.parsing.Rules.guard;
import static org.zwobble.shed.compiler.parsing.Rules.oneOrMoreWithSeparator;
import static org.zwobble.shed.compiler.parsing.Rules.optional;
import static org.zwobble.shed.compiler.parsing.Rules.sequence;
import static org.zwobble.shed.compiler.parsing.Rules.symbol;
import static org.zwobble.shed.compiler.parsing.Rules.then;
import static org.zwobble.shed.compiler.parsing.Rules.tokenOfType;
import static org.zwobble.shed.compiler.parsing.Rules.whitespace;
import static org.zwobble.shed.compiler.parsing.Separator.hardSeparator;
import static org.zwobble.shed.compiler.tokeniser.TokenType.IDENTIFIER;


public class TypeReferences {
    @SuppressWarnings("unchecked")
    public static Rule<TypeReferenceNode> typeReference() {
        return new Rule<TypeReferenceNode>() {
            @Override
            public Result<TypeReferenceNode> parse(TokenIterator tokens) {
                return firstOf("type reference",
                    typeApplication(),
                    typeIdentifier()
                ).parse(tokens);
            }
        };
    }
    
    public static Rule<TypeIdentifierNode> typeIdentifier() {
        return then(
            tokenOfType(IDENTIFIER),
            new ParseAction<String, TypeIdentifierNode>() {
                @Override
                public TypeIdentifierNode apply(String result) {
                    return new TypeIdentifierNode(result);
                }
            }
        );
    }
    
    public static Rule<TypeApplicationNode> typeApplication() {
        final Rule<TypeIdentifierNode> baseIdentifier;
        final Rule<List<TypeReferenceNode>> typeParameters = oneOrMoreWithSeparator(typeReference(), hardSeparator(symbol(",")));
        return then(
            sequence(OnError.FINISH,
                baseIdentifier = guard(typeIdentifier()),
                guard(symbol("[")),
                typeParameters,
                symbol("]")
            ),
            new ParseAction<RuleValues, TypeApplicationNode>() {
                @Override
                public TypeApplicationNode apply(RuleValues result) {
                    return new TypeApplicationNode(result.get(baseIdentifier), result.get(typeParameters));
                }
            }
        );
    }
    
    public static Rule<TypeReferenceNode> typeSpecifier() {
        final Rule<TypeReferenceNode> typeReference = typeReference();
        return then(
            sequence(OnError.FINISH,
                guard(symbol(":")),
                optional(whitespace()),
                typeReference
            ),
            new ParseAction<RuleValues, TypeReferenceNode>() {
                @Override
                public TypeReferenceNode apply(RuleValues result) {
                    return result.get(typeReference);
                }
            }
        );
    }
}
