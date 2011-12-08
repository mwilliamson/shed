package org.zwobble.shed.compiler.typebinding;

import org.junit.Test;
import org.zwobble.shed.compiler.metaclassgeneration.MetaClasses;
import org.zwobble.shed.compiler.parsing.nodes.InterfaceDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typegeneration.TypeStore;
import org.zwobble.shed.compiler.types.InterfaceType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.typechecker.ShedTypeValue.shedTypeValue;

public class TypeBinderTest {
    @Test public void
    typeBinderBindsGeneratedTypesInStaticContext() {
        InterfaceDeclarationNode typeDeclaration = Nodes.interfaceDeclaration("Person", Nodes.interfaceBody());
        MetaClasses metaClasses = MetaClasses.create();
        StaticContext context = new StaticContext(metaClasses);
        TypeBinder typeBinder = new TypeBinder(metaClasses);
        TypeStore.Builder typeStore = TypeStore.builder();
        InterfaceType type = new InterfaceType(fullyQualifiedName("Person"));
        typeStore.add(typeDeclaration, type);
        
        typeBinder.bindTypes(typeStore.build(), context);
        
        ValueInfo expectedValueInfo = ValueInfo.unassignableValue(metaClasses.metaClassOf(type), shedTypeValue(type));
        assertThat(context.getValueInfoFor(typeDeclaration), is(some(expectedValueInfo)));
    }
}
