package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.parsing.nodes.StatementNode;

public interface HoistableStatementTypeChecker<T extends StatementNode> extends StatementTypeChecker<T>, StatementForwardDeclarer<T>  {

}
