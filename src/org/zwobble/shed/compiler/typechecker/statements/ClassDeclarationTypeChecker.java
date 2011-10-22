package org.zwobble.shed.compiler.typechecker.statements;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.naming.FullyQualifiedNames;
import org.zwobble.shed.compiler.parsing.nodes.ClassDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.typechecker.BlockTypeChecker;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeLookup;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typechecker.VariableLookupResult;
import org.zwobble.shed.compiler.typechecker.VariableLookupResult.Status;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.typechecker.ShedTypeValue.shedTypeValue;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.Members.members;

public class ClassDeclarationTypeChecker implements DeclarationTypeChecker<ClassDeclarationNode> {
    private final BlockTypeChecker blockTypeChecker;
    private final TypeLookup typeLookup;
    private final FullyQualifiedNames fullyQualifiedNames;
    private final StaticContext context;

    @Inject
    public ClassDeclarationTypeChecker(BlockTypeChecker blockTypeChecker, TypeLookup typeLookup, FullyQualifiedNames fullyQualifiedNames, StaticContext context) {
        this.blockTypeChecker = blockTypeChecker;
        this.typeLookup = typeLookup;
        this.fullyQualifiedNames = fullyQualifiedNames;
        this.context = context;
    }
    
    @Override
    public TypeResult<?> forwardDeclare(ClassDeclarationNode classDeclaration) {
        TypeResult<?> result = forwardDeclareBody(classDeclaration);
        buildClassType(classDeclaration);
        return result;
    }

    private TypeResult<?> forwardDeclareBody(ClassDeclarationNode classDeclaration) {
        return blockTypeChecker.forwardDeclare(classDeclaration.getBody());
    }

    private void buildClassType(ClassDeclarationNode classDeclaration) {
        FullyQualifiedName name = fullyQualifiedNames.fullyQualifiedNameOf(classDeclaration);
        Map<String, ValueInfo> members = buildMembers(classDeclaration);
        ClassType type = new ClassType(name);
        List<Type> functionTypeParameters = newArrayList(transform(classDeclaration.getFormalArguments(), toType()));
        functionTypeParameters.add(type);
        // TODO: forbid user-declared members called Meta 
        FullyQualifiedName metaClassName = name.extend("Meta");
        ClassType metaClass = new ClassType(metaClassName);
        ScalarTypeInfo metaClassTypeInfo = new ScalarTypeInfo(interfaces(CoreTypes.functionTypeOf(functionTypeParameters), CoreTypes.classOf(type)), members());
        
        context.add(classDeclaration, ValueInfo.unassignableValue(metaClass, shedTypeValue(type)));
        context.addInfo(type, new ScalarTypeInfo(interfaces(), members));
        context.addInfo(metaClass, metaClassTypeInfo);
    }

    @Override
    public TypeResult<StatementTypeCheckResult> typeCheck(ClassDeclarationNode classDeclaration, Option<Type> returnType) {
        TypeResult<StatementTypeCheckResult> result = blockTypeChecker.typeCheck(classDeclaration.getBody(), returnType);
        buildClassType(classDeclaration);
        return result;
    }

    private Map<String, ValueInfo> buildMembers(ClassDeclarationNode classDeclaration) {
        ImmutableMap.Builder<String, ValueInfo> members = ImmutableMap.builder();
        
        Iterable<PublicDeclarationNode> publicDeclarations = Iterables.filter(classDeclaration.getBody(), PublicDeclarationNode.class);
        for (PublicDeclarationNode publicDeclaration : publicDeclarations) {
            DeclarationNode memberDeclaration = publicDeclaration.getDeclaration();
            Option<ValueInfo> memberType = findMemberType(memberDeclaration);
            if (memberType.hasValue()) {
                members.put(memberDeclaration.getIdentifier(), memberType.get());
            }
        }
        return members.build();
    }

    private Option<ValueInfo> findMemberType(DeclarationNode memberDeclaration) {
        VariableLookupResult result = context.get(memberDeclaration);
        if (result.getStatus() == Status.SUCCESS) {
            return some(result.getValueInfo());
        } else {
            return none();
        }
    }

    private Function<FormalArgumentNode, Type> toType() {
        return new Function<FormalArgumentNode, Type>() {
            @Override
            public Type apply(FormalArgumentNode input) {
                TypeResult<Type> lookupResult = typeLookup.lookupTypeReference(input.getType());
                if (!lookupResult.isSuccess()) {
                    // TODO:
                    throw new RuntimeException("Failed type lookup");
                }
                return lookupResult.get();
            }
        };
    }
}
