package org.zwobble.shed.compiler.typechecker.statements;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.naming.FullyQualifiedNamesBuilder;
import org.zwobble.shed.compiler.parsing.nodes.ClassDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.referenceresolution.ReferencesBuilder;
import org.zwobble.shed.compiler.typechecker.SimpleNodeLocations;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeCheckerInjector;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typechecker.statements.ClassDeclarationTypeChecker;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.Type;

import com.google.inject.Injector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.isSuccess;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;

public class ClassDeclarationTypeCheckerTest {
    private static final Map<String, ValueInfo> NO_MEMBERS = Collections.<String, ValueInfo>emptyMap();
    private static final Set<InterfaceType> NO_INTERFACES = Collections.<InterfaceType>emptySet();
    private final SimpleNodeLocations nodeLocations = new SimpleNodeLocations();
    private final ReferencesBuilder references = new ReferencesBuilder();
    private final FullyQualifiedNamesBuilder fullNames = new FullyQualifiedNamesBuilder();
    private final StaticContext context = StaticContext.defaultContext(references.build());
    
    @Test public void
    classTypeIsBuiltInForwardDeclarationWithNameOfClass() {
        ClassDeclarationNode declaration = Nodes.clazz("Browser", Nodes.noFormalArguments(), Nodes.block());
        FullyQualifiedName fullyQualifiedName = fullyQualifiedName("shed", "Browser");
        fullNames.addFullyQualifiedName(declaration, fullyQualifiedName);
        TypeResult<?> result = forwardDeclare(declaration);
        assertThat(result, isSuccess());
        assertThat(context.get(declaration).getType(), is((Type)new ClassType(fullyQualifiedName, NO_INTERFACES, NO_MEMBERS)));
    }
    
    private TypeResult<?> forwardDeclare(ClassDeclarationNode classDeclaration) {
        Injector injector = TypeCheckerInjector.build(nodeLocations, fullNames.build());
        ClassDeclarationTypeChecker typeChecker = injector.getInstance(ClassDeclarationTypeChecker.class);
        return typeChecker.forwardDeclare(classDeclaration, context);
    }
}
