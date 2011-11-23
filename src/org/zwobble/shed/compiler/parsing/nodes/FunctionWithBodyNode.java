package org.zwobble.shed.compiler.parsing.nodes;


public interface FunctionWithBodyNode extends FunctionNode {
    BlockNode getBody();
}
