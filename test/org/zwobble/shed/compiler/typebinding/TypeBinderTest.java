package org.zwobble.shed.compiler.typebinding;

import org.junit.Test;
import org.zwobble.shed.compiler.metaclassgeneration.MetaClasses;
import org.zwobble.shed.compiler.parsing.nodes.ClassDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.InterfaceDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.ObjectDeclarationNode;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typegeneration.TypeStore;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.InterfaceType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.typechecker.ShedTypeValue.shedTypeValue;

public class TypeBinderTest {
    private final MetaClasses metaClasses = MetaClasses.create();
    private final StaticContext context = new StaticContext(metaClasses);
    private final TypeBinder typeBinder = new TypeBinder(metaClasses);
    private final TypeStore.Builder typeStore = TypeStore.builder();
    
    @Test public void
    typeBinderBindsGeneratedMetaClassForInterfaceDeclarations() {
        InterfaceDeclarationNode typeDeclaration = Nodes.interfaceDeclaration("Person", Nodes.interfaceBody());
        InterfaceType type = new InterfaceType(fullyQualifiedName("Person"));
        typeStore.add(typeDeclaration, type);
        
        typeBinder.bindTypes(typeStore.build(), context);
        
        ValueInfo expectedValueInfo = ValueInfo.unassignableValue(metaClasses.metaClassOf(type), shedTypeValue(type));
        assertThat(context.getValueInfoFor(typeDeclaration), is(some(expectedValueInfo)));
    }
    
    @Test public void
    typeBinderBindsGeneratedMetaClassForClassDeclarations() {
        ClassDeclarationNode typeDeclaration = Nodes.clazz("Person", Nodes.formalArguments(), Nodes.block());
        ClassType type = new ClassType(fullyQualifiedName("Person"));
        typeStore.add(typeDeclaration, type);
        
        typeBinder.bindTypes(typeStore.build(), context);
        
        ValueInfo expectedValueInfo = ValueInfo.unassignableValue(metaClasses.metaClassOf(type), shedTypeValue(type));
        assertThat(context.getValueInfoFor(typeDeclaration), is(some(expectedValueInfo)));
    }
    
    @Test public void
    typeBinderBindsGeneratedTypeForObjectDeclarations() {
        ObjectDeclarationNode objectDeclaration = Nodes.object("Person", Nodes.block());
        ClassType type = new ClassType(fullyQualifiedName("Person"));
        typeStore.add(objectDeclaration, type);
        
        typeBinder.bindTypes(typeStore.build(), context);
        
        ValueInfo expectedValueInfo = ValueInfo.unassignableValue(type);
        assertThat(context.getValueInfoFor(objectDeclaration), is(some(expectedValueInfo)));
    }
}
