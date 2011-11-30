package org.zwobble.shed.compiler.typechecker.statements;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.parsing.nodes.BlockNode;
import org.zwobble.shed.compiler.parsing.nodes.ClassDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Declaration;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.typechecker.ShedTypeValue;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeCheckerTestFixture;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typechecker.errors.MissingMemberError;
import org.zwobble.shed.compiler.typechecker.errors.TypeMismatchError;
import org.zwobble.shed.compiler.typechecker.errors.UntypedReferenceError;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.Member;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.Types;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;
import static org.zwobble.shed.compiler.CompilerTesting.isSuccess;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration.globalDeclaration;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
import static org.zwobble.shed.compiler.types.CoreTypes.functionTypeOf;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.Member.member;
import static org.zwobble.shed.compiler.types.Members.members;

public class ClassDeclarationTypeCheckerTest {
    private final TypeCheckerTestFixture fixture = TypeCheckerTestFixture.build();
    private final FullyQualifiedName fullyQualifiedName = fullyQualifiedName("shed", "Browser");
    private final ClassType type = new ClassType(fullyQualifiedName);
    
    @Test public void
    classTypeIsAlsoConstructor() {
        ClassDeclarationNode declaration = Nodes.clazz("Browser", asList(Nodes.formalArgument("name", fixture.stringTypeReference())), Nodes.block());
        fixture.addType(declaration, type);
        StaticContext context = fixture.context();
        
        TypeResult<?> result = forwardDeclare(declaration);
        
        assertThat(result, isSuccess());
        ScalarType metaClass = (ScalarType) context.get(declaration).getType();
        ScalarTypeInfo metaClassInfo = context.getInfo(metaClass);
        assertThat(metaClassInfo.getInterfaces(), hasItem(CoreTypes.functionTypeOf(CoreTypes.STRING, type)));
    }
    
    @Test public void
    classTypeIsAnInstanceOfClass() {
        ClassDeclarationNode declaration = Nodes.clazz("Browser", asList(Nodes.formalArgument("name", fixture.stringTypeReference())), Nodes.block());
        fixture.addType(declaration, type);
        StaticContext context = fixture.context();
        
        TypeResult<?> result = forwardDeclare(declaration);
        
        assertThat(result, isSuccess());
        ScalarType metaClass = (ScalarType) context.get(declaration).getType();
        ScalarTypeInfo metaClassInfo = context.getInfo(metaClass);
        assertThat(metaClassInfo.getInterfaces(), hasItem(CoreTypes.CLASS));
    }
    
    @Test public void
    classTypeIsBuiltInForwardDeclarationWithMembersThatCanBeTypedWithoutTypingEntireBody() {
        BlockNode body = Nodes.block(
            Nodes.publik(Nodes.immutableVar("firstName", fixture.stringTypeReference(), Nodes.string("Bob"))),
            Nodes.publik(Nodes.func("close", Nodes.noFormalArguments(), fixture.unitTypeReference(), Nodes.block(Nodes.returnStatement(Nodes.unit())))),
            Nodes.immutableVar("lastName", fixture.stringTypeReference(), Nodes.string("Bobertson"))
        );
        ClassDeclarationNode declaration = Nodes.clazz("Person", Nodes.noFormalArguments(), body);
        fixture.addType(declaration, type);
        StaticContext context = fixture.context();
        
        TypeResult<?> result = forwardDeclare(declaration);
        
        assertThat(result, isSuccess());
        ShedTypeValue value = (ShedTypeValue) context.get(declaration).getValue().get();
        ClassType type = (ClassType)value.getType();
        ScalarTypeInfo typeInfo = context.getInfo(type);
        assertThat(typeInfo.getMembers(), contains(
            member("firstName", ValueInfo.unassignableValue(CoreTypes.STRING)),
            member("close", ValueInfo.unassignableValue(CoreTypes.functionTypeOf(CoreTypes.UNIT)))
        ));
    }
    
    @Test public void
    membersThatCannotBeForwardDeclaredHaveUnknownType() {
        BlockNode body = Nodes.block(
            Nodes.publik(Nodes.immutableVar("firstName", Nodes.string("Bob")))
        );
        ClassDeclarationNode declaration = Nodes.clazz("Person", Nodes.noFormalArguments(), body);
        fixture.addType(declaration, type);
        StaticContext context = fixture.context();
        
        TypeResult<?> result = forwardDeclare(declaration);
        
        assertThat(result, isSuccess());
        ShedTypeValue value = (ShedTypeValue) context.get(declaration).getValue().get();
        ClassType type = (ClassType)value.getType();
        ScalarTypeInfo typeInfo = context.getInfo(type);
        assertThat(typeInfo.getMembers(), contains(memberOfUnknownType("firstName")));
    }

    @Test public void
    errorsArePassedAlongFromForwardDeclaringMembers() {
        BlockNode body = Nodes.block(
            Nodes.publik(Nodes.immutableVar("firstName", Nodes.id("Blah"), Nodes.string("Bob")))
        );
        ClassDeclarationNode declaration = Nodes.clazz("Person", Nodes.noFormalArguments(), body);
        fixture.addType(declaration, type);
        
        TypeResult<?> result = forwardDeclare(declaration);
        
        assertThat(result, isFailureWithErrors(new UntypedReferenceError("Blah")));
    }
    
    @Test public void
    bodyOfClassIsTypeChecked() {
        BlockNode body = Nodes.block(
            Nodes.immutableVar("firstName", fixture.unitTypeReference(), Nodes.string("Bob"))
        );
        ClassDeclarationNode declaration = Nodes.clazz("Person", Nodes.noFormalArguments(), body);
        fixture.addType(declaration, type);
        forwardDeclare(declaration);
        TypeResult<?> result = typeCheck(declaration);
        
        assertThat(result, isFailureWithErrors(new TypeMismatchError(CoreTypes.UNIT, CoreTypes.STRING)));
    }
    
    @Test public void
    classArgumentsAreAddedToContextOfBody() {
        FormalArgumentNode formalArgument = Nodes.formalArgument("initialAge", fixture.doubleTypeReference());
        VariableIdentifierNode argumentReference = Nodes.id("initialAge");
        BlockNode body = Nodes.block(
            Nodes.mutableVar("age", fixture.doubleTypeReference(), argumentReference)
        );
        ClassDeclarationNode declaration = Nodes.clazz("Person", asList(formalArgument), body);
        fixture.addType(declaration, type);
        fixture.addReference(argumentReference, formalArgument);
        forwardDeclare(declaration);
        TypeResult<?> result = typeCheck(declaration);
        
        assertThat(result, isSuccess());
    }
    
    @Test public void
    errorIfTypeOfFormalArgumentCannotBeFound() {
        FormalArgumentNode formalArgument = Nodes.formalArgument("initialAge", Nodes.id("Age"));
        VariableIdentifierNode argumentReference = Nodes.id("initialAge");
        ClassDeclarationNode declaration = Nodes.clazz("Person", asList(formalArgument), Nodes.block());
        fixture.addType(declaration, type);
        fixture.addReference(argumentReference, formalArgument);
        forwardDeclare(declaration);
        TypeResult<?> result = typeCheck(declaration);
        
        assertThat(result, isFailureWithErrors(new UntypedReferenceError("Age")));
    }
    
    @Test public void
    superTypesOfClassAreChecked() {
        InterfaceType interfaceType = new InterfaceType(fullyQualifiedName("Store"));
        VariableIdentifierNode interfaceReference = Nodes.id("Store");
        Declaration interfaceDeclaration = globalDeclaration("Store");
        fixture.addReference(interfaceReference, interfaceDeclaration);
        
        ClassDeclarationNode declaration = Nodes.clazz("Account", Nodes.noFormalArguments(), asList((ExpressionNode)interfaceReference), Nodes.block());
        fixture.addType(declaration, type);
        StaticContext context = fixture.context();
        ScalarType interfaceFunctionType = functionTypeOf(fixture.implementingClassType(), CoreTypes.UNIT);
        ScalarTypeInfo interfaceTypeInfo = new ScalarTypeInfo(interfaces(), members("add", unassignableValue(interfaceFunctionType)));
        context.addInterface(interfaceDeclaration, interfaceType, interfaceTypeInfo);
        context.add(interfaceDeclaration, ValueInfo.unassignableValue(fixture.metaClasses().metaClassOf(interfaceType)));
        
        forwardDeclareSuccessfully(declaration);
        TypeResult<?> result = typeCheck(declaration);
        
        assertThat(result, isFailureWithErrors(new MissingMemberError(interfaceType, "add")));
    }
    
    @Test public void
    errorIfInterfaceCannotBeFound() {
        ClassDeclarationNode declaration = Nodes.clazz("Person", Nodes.noFormalArguments(), asList((ExpressionNode)Nodes.id("Data")), Nodes.block());
        fixture.addType(declaration, type);
        forwardDeclare(declaration);
        TypeResult<?> result = typeCheck(declaration);
        
        assertThat(result, isFailureWithErrors(new UntypedReferenceError("Data")));
    }
    
    private void forwardDeclareSuccessfully(ClassDeclarationNode classDeclaration) {
        assertThat(typeChecker().forwardDeclare(classDeclaration), isSuccess());
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
    
    private Matcher<Member> memberOfUnknownType(final String name) {
        return new TypeSafeDiagnosingMatcher<Member>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Member " + name + " with unknown type");
            }

            @Override
            protected boolean matchesSafely(Member item, Description mismatchDescription) {
                if (!item.getName().equals(name)) {
                    mismatchDescription.appendText("had name " + item.getName());
                    return false;
                }
                if (Types.isUnknown(item.getType())) {
                    return true;
                } else {
                    mismatchDescription.appendText("was of type " + item.getType());
                    return false;
                }
            }
        };
    }
}
