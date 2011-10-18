package org.zwobble.shed.compiler.typechecker.statements;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.naming.FullyQualifiedNames;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ObjectDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.typechecker.BlockTypeChecker;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;

public class ObjectDeclarationTypeChecker implements StatementTypeChecker<ObjectDeclarationNode> {
    private final BlockTypeChecker blockTypeChecker;
    private final FullyQualifiedNames fullyQualifiedNames;
    private final StaticContext context;

    @Inject
    public ObjectDeclarationTypeChecker(BlockTypeChecker blockTypeChecker, FullyQualifiedNames fullyQualifiedNames, StaticContext context) {
        this.blockTypeChecker = blockTypeChecker;
        this.fullyQualifiedNames = fullyQualifiedNames;
        this.context = context;
    }
    
    @Override
    public TypeResult<StatementTypeCheckResult> typeCheck(ObjectDeclarationNode objectDeclaration, Option<Type> returnType) {
        TypeResult<StatementTypeCheckResult> result = TypeResult.success(StatementTypeCheckResult.noReturn());

        TypeResult<StatementTypeCheckResult> blockResult = 
            blockTypeChecker.forwardDeclareAndTypeCheck(objectDeclaration.getStatements(), Option.<Type>none());
        result = result.withErrorsFrom(blockResult);
        
        if (result.isSuccess()) {
            Builder<String, ValueInfo> memberBuilder = ImmutableMap.builder();

            for (StatementNode statement : objectDeclaration.getStatements()) {
                if (statement instanceof PublicDeclarationNode) {
                    DeclarationNode declaration = ((PublicDeclarationNode) statement).getDeclaration();
                    memberBuilder.put(declaration.getIdentifier(), context.get(declaration).getValueInfo());
                }
            }
            
            InterfaceType type = new InterfaceType(fullyQualifiedNames.fullyQualifiedNameOf(objectDeclaration));
            context.add(objectDeclaration, unassignableValue(type));
            context.addInfo(type, new ScalarTypeInfo(interfaces(), memberBuilder.build()));
        }
        
        return result;
    }
    
}
