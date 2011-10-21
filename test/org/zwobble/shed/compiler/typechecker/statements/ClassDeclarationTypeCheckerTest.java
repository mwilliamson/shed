package org.zwobble.shed.compiler.typechecker.statements;

import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.parsing.nodes.BlockNode;
import org.zwobble.shed.compiler.parsing.nodes.ClassDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeCheckerTestFixture;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typechecker.errors.TypeMismatchError;
import org.zwobble.shed.compiler.typechecker.errors.UntypedReferenceError;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.collect.ImmutableMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;
import static org.zwobble.shed.compiler.CompilerTesting.isSuccess;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.typechecker.TypeCheckerTestFixture.STRING_TYPE_REFERENCE;
import static org.zwobble.shed.compiler.typechecker.TypeCheckerTestFixture.UNIT_TYPE_REFERENCE;

public class ClassDeclarationTypeCheckerTest {
    private final TypeCheckerTestFixture fixture = TypeCheckerTestFixture.build();
    
    @Test public void
    classTypeIsBuiltInForwardDeclarationWithNameOfClass() {
        ClassDeclarationNode declaration = Nodes.clazz("Browser", Nodes.noFormalArguments(), Nodes.block());
        FullyQualifiedName fullyQualifiedName = fullyQualifiedName("shed", "Browser");
        fixture.addFullyQualifiedName(declaration, fullyQualifiedName);
        StaticContext context = fixture.context();
        
        TypeResult<?> result = forwardDeclare(declaration);
        
        assertThat(result, isSuccess());
        ClassType type = (ClassType) context.get(declaration).getType();
        assertThat(type.getFullyQualifiedName(), is(fullyQualifiedName));
    }
    
    @Test public void
    classTypeIsBuiltInForwardDeclarationWithMembersThatCanBeTypedWithoutTypingEntireBody() {
        BlockNode body = Nodes.block(
            Nodes.publik(Nodes.immutableVar("firstName", STRING_TYPE_REFERENCE, Nodes.string("Bob"))),
            Nodes.publik(Nodes.func("close", Nodes.noFormalArguments(), UNIT_TYPE_REFERENCE, Nodes.block(Nodes.returnStatement(Nodes.unit())))),
            Nodes.immutableVar("lastName", STRING_TYPE_REFERENCE, Nodes.string("Bobertson"))
        );
        ClassDeclarationNode declaration = Nodes.clazz("Person", Nodes.noFormalArguments(), body);
        FullyQualifiedName fullyQualifiedName = fullyQualifiedName("shed", "Person");
        fixture.addFullyQualifiedName(declaration, fullyQualifiedName);
        StaticContext context = fixture.context();
        
        TypeResult<?> result = forwardDeclare(declaration);
        
        assertThat(result, isSuccess());
        ClassType type = (ClassType) context.get(declaration).getType();
        ScalarTypeInfo typeInfo = context.getInfo(type);
        assertThat(typeInfo.getMembers(), Matchers.<Map<String, ValueInfo>>is(ImmutableMap.of(
            "firstName", ValueInfo.unassignableValue(CoreTypes.STRING),
            "close", ValueInfo.unassignableValue(CoreTypes.functionTypeOf(CoreTypes.UNIT))
        )));
    }
    
    @Test public void
    membersThatCannotBeForwardDeclaredHaveUnknownType() {
        BlockNode body = Nodes.block(
            Nodes.publik(Nodes.immutableVar("firstName", Nodes.string("Bob")))
        );
        ClassDeclarationNode declaration = Nodes.clazz("Person", Nodes.noFormalArguments(), body);
        FullyQualifiedName fullyQualifiedName = fullyQualifiedName("shed", "Person");
        fixture.addFullyQualifiedName(declaration, fullyQualifiedName);
        StaticContext context = fixture.context();
        
        TypeResult<?> result = forwardDeclare(declaration);
        
        assertThat(result, isSuccess());
        ClassType type = (ClassType) context.get(declaration).getType();
        ScalarTypeInfo typeInfo = context.getInfo(type);
        assertThat(typeInfo.getMembers(), Matchers.<Map<String, ValueInfo>>is(ImmutableMap.of(
            "firstName", ValueInfo.unknown()
        )));
    }
    
    @Test public void
    errorsArePassedAlongFromForwardDeclaringMembers() {
        BlockNode body = Nodes.block(
            Nodes.publik(Nodes.immutableVar("firstName", Nodes.id("Blah"), Nodes.string("Bob")))
        );
        ClassDeclarationNode declaration = Nodes.clazz("Person", Nodes.noFormalArguments(), body);
        FullyQualifiedName fullyQualifiedName = fullyQualifiedName("shed", "Person");
        fixture.addFullyQualifiedName(declaration, fullyQualifiedName);
        
        TypeResult<?> result = forwardDeclare(declaration);
        
        assertThat(result, isFailureWithErrors(new UntypedReferenceError("Blah")));
    }
    
    @Test public void
    bodyOfClassIsTypeChecked() {
        BlockNode body = Nodes.block(
            Nodes.immutableVar("firstName", UNIT_TYPE_REFERENCE, Nodes.string("Bob"))
        );
        ClassDeclarationNode declaration = Nodes.clazz("Person", Nodes.noFormalArguments(), body);
        FullyQualifiedName fullyQualifiedName = fullyQualifiedName("shed", "Person");
        fixture.addFullyQualifiedName(declaration, fullyQualifiedName);
        forwardDeclare(declaration);
        TypeResult<?> result = typeCheck(declaration);
        
        assertThat(result, isFailureWithErrors(new TypeMismatchError(CoreTypes.UNIT, CoreTypes.STRING)));
    }
    
    @Test public void
    classTypeIsBuiltInTypeCheckingWithAllMembers() {
        BlockNode body = Nodes.block(
            Nodes.publik(Nodes.immutableVar("firstName", STRING_TYPE_REFERENCE, Nodes.string("Bob"))),
            Nodes.publik(Nodes.func("close", Nodes.noFormalArguments(), UNIT_TYPE_REFERENCE, Nodes.block(Nodes.returnStatement(Nodes.unit())))),
            Nodes.publik(Nodes.immutableVar("lastName", Nodes.string("Bobertson")))
        );
        ClassDeclarationNode declaration = Nodes.clazz("Person", Nodes.noFormalArguments(), body);
        FullyQualifiedName fullyQualifiedName = fullyQualifiedName("shed", "Person");
        fixture.addFullyQualifiedName(declaration, fullyQualifiedName);
        StaticContext context = fixture.context();
        
        forwardDeclare(declaration);
        typeCheck(declaration);
        
        ClassType type = (ClassType) context.get(declaration).getType();
        ScalarTypeInfo typeInfo = context.getInfo(type);
        assertThat(typeInfo.getMembers(), Matchers.<Map<String, ValueInfo>>is(ImmutableMap.of(
            "firstName", ValueInfo.unassignableValue(CoreTypes.STRING),
            "close", ValueInfo.unassignableValue(CoreTypes.functionTypeOf(CoreTypes.UNIT)),
            "lastName", ValueInfo.unassignableValue(CoreTypes.STRING)
        )));
    }
    
    private TypeResult<?> forwardDeclare(ClassDeclarationNode classDeclaration) {
        return typeChecker().forwardDeclare(classDeclaration);
    }
    
    private TypeResult<?> typeCheck(ClassDeclarationNode classDeclaration) {
        return typeChecker().typeCheck(classDeclaration, Option.<Type>none());
    }

    private ClassDeclarationTypeChecker typeChecker() {
        return fixture.get(ClassDeclarationTypeChecker.class);
    }
}