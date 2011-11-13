package org.zwobble.shed.compiler.parsing;

import java.util.List;

import lombok.Data;

import org.zwobble.shed.compiler.parsing.nodes.BlockNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionSignatureDeclarationNode;
import org.zwobble.shed.compiler.tokeniser.Keyword;

import static org.zwobble.shed.compiler.parsing.Statements.aStatement;

import static org.zwobble.shed.compiler.parsing.Rules.guard;
import static org.zwobble.shed.compiler.parsing.Rules.keyword;
import static org.zwobble.shed.compiler.parsing.Rules.sequence;
import static org.zwobble.shed.compiler.parsing.Rules.then;
import static org.zwobble.shed.compiler.parsing.Rules.tokenOfType;
import static org.zwobble.shed.compiler.tokeniser.TokenType.IDENTIFIER;

public class Functions {
    public static Rule<FunctionSignatureDeclarationNode> functionSignatureDeclaration() {
        final Rule<FunctionSignature> signature = guard(signature());
        return then(
            aStatement(signature),
            new SimpleParseAction<RuleValues, FunctionSignatureDeclarationNode>() {
                @Override
                public FunctionSignatureDeclarationNode apply(RuleValues result) {
                    FunctionSignature signatureValue = result.get(signature);
                    return new FunctionSignatureDeclarationNode(
                        signatureValue.getIdentifier(),
                        signatureValue.getFormalArguments(),
                        signatureValue.getReturnType()
                    );
                }
            }
        );
    }

    public static Rule<FunctionDeclarationNode> functionDeclaration() {
        final Rule<FunctionSignature> signatureRule = guard(signature());
        final Rule<BlockNode> body = Blocks.block();
        return then(
            sequence(OnError.FINISH,
                signatureRule,
                body
            ),
            new SimpleParseAction<RuleValues, FunctionDeclarationNode>() {
                @Override
                public FunctionDeclarationNode apply(RuleValues result) {
                    FunctionSignature signature = result.get(signatureRule);
                    return new FunctionDeclarationNode(
                        signature.getIdentifier(),
                        signature.getFormalArguments(),
                        signature.getReturnType(),
                        result.get(body)
                    );
                }
            }
        );
    }
    
    private static Rule<FunctionSignature> signature() {
        final Rule<String> identifier = tokenOfType(IDENTIFIER);
        final Rule<List<FormalArgumentNode>> formalArguments = Arguments.formalArgumentList();
        final Rule<ExpressionNode> returnType = TypeReferences.typeSpecifier();
        return then(
            sequence(OnError.FINISH,
                guard(keyword(Keyword.FUN)),
                identifier,
                formalArguments,
                returnType
            ),
            new SimpleParseAction<RuleValues, FunctionSignature>() {
                @Override
                public FunctionSignature apply(RuleValues result) {
                    return new FunctionSignature(
                        result.get(identifier),
                        result.get(formalArguments),
                        result.get(returnType)
                    );
                }
            }
        );
    }
    
    @Data
    private static class FunctionSignature {
        private final String identifier;
        private final List<FormalArgumentNode> formalArguments;
        private final ExpressionNode returnType;
    }
}
