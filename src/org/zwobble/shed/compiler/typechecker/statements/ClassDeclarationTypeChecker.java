package org.zwobble.shed.compiler.typechecker.statements;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.naming.FullyQualifiedNames;
import org.zwobble.shed.compiler.parsing.nodes.ClassDeclarationNode;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.Type;

public class ClassDeclarationTypeChecker implements HoistableStatementTypeChecker<ClassDeclarationNode> {
    private final FullyQualifiedNames fullyQualifiedNames;

    @Inject
    public ClassDeclarationTypeChecker(FullyQualifiedNames fullyQualifiedNames) {
        this.fullyQualifiedNames = fullyQualifiedNames;
    }
    
    @Override
    public TypeResult<?> forwardDeclare(
        ClassDeclarationNode statement, StaticContext context
    ) {
        FullyQualifiedName name = fullyQualifiedNames.fullyQualifiedNameOf(statement);
        Set<InterfaceType> interfaces = Collections.<InterfaceType>emptySet();
        Map<String, ValueInfo> members = Collections.<String, ValueInfo>emptyMap();
        ClassType type = new ClassType(name, interfaces, members);
        context.add(statement, ValueInfo.unassignableValue(type));
        return TypeResult.success();
    }

    @Override
    public TypeResult<StatementTypeCheckResult> typeCheck(
        ClassDeclarationNode statement, StaticContext context, Option<Type> returnType
    ) {
        return null;
    }

}
