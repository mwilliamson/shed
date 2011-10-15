package org.zwobble.shed.compiler.typechecker.statements;

import org.zwobble.shed.compiler.parsing.nodes.StatementNode;

public interface DeclarationTypeChecker<T extends StatementNode> extends StatementTypeChecker<T>, StatementForwardDeclarer<T>  {

}
