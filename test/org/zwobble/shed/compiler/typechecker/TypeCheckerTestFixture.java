package org.zwobble.shed.compiler.typechecker;

import java.util.Map;

import org.zwobble.shed.compiler.parsing.nodes.Declaration;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.TypeDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.referenceresolution.ReferencesBuilder;
import org.zwobble.shed.compiler.typegeneration.TypeStore;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration.globalDeclaration;

import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.Members.members;

public class TypeCheckerTestFixture {
    private static VariableIdentifierNode STRING_TYPE_REFERENCE = Nodes.id("String");
    private static VariableIdentifierNode UNIT_TYPE_REFERENCE = Nodes.id("Unit");
    private static VariableIdentifierNode BOOLEAN_TYPE_REFERENCE = Nodes.id("Boolean");
    private static VariableIdentifierNode DOUBLE_TYPE_REFERENCE = Nodes.id("Double");
    
    public static TypeCheckerTestFixture build() {
        return new TypeCheckerTestFixture();
    }
    
    private final TypeStore.Builder typeStoreBuilder = TypeStore.builder();
    private final StaticContext context;
    private final ReferencesBuilder references = new ReferencesBuilder();
    
    private final Declaration stringTypeDeclaration;
    private final Declaration unitTypeDeclaration;
    private final Declaration booleanTypeDeclaration;
    private final Declaration doubleTypeDeclaration;

    private final InterfaceType interfaceType = new InterfaceType(fullyQualifiedName("HasAuthor"));
    private final ClassType implementingClassType = new ClassType(fullyQualifiedName("Song"));
    private final Declaration interfaceTypeDeclaration = globalDeclaration("HasAuthor");
    private final Declaration implementingClassTypeDeclaration = globalDeclaration("Song");
    private final VariableIdentifierNode interfaceTypeReference = Nodes.id("HasAuthor");
    private final VariableIdentifierNode implementingClassTypeReference = Nodes.id("Song");
    
    private TypeCheckerTestFixture() {
        context = DefaultContext.defaultContext();
        
        Map<String, GlobalDeclaration> builtIns = context.getBuiltIns();
        stringTypeDeclaration = builtIns.get("String");
        unitTypeDeclaration = builtIns.get("Unit");
        booleanTypeDeclaration = builtIns.get("Boolean");
        doubleTypeDeclaration = builtIns.get("Double");
        
        references.addReference(STRING_TYPE_REFERENCE, stringTypeDeclaration);
        references.addReference(UNIT_TYPE_REFERENCE, unitTypeDeclaration);
        references.addReference(BOOLEAN_TYPE_REFERENCE, booleanTypeDeclaration);
        references.addReference(DOUBLE_TYPE_REFERENCE, doubleTypeDeclaration);
        
        references.addReference(implementingClassTypeReference, implementingClassTypeDeclaration);
        references.addReference(interfaceTypeReference, interfaceTypeDeclaration);
        context.addClass(implementingClassTypeDeclaration, implementingClassType, new ScalarTypeInfo(interfaces(interfaceType), members()));
        context.addInterface(interfaceTypeDeclaration, interfaceType, ScalarTypeInfo.EMPTY);
    }

    public <T> T get(Class<T> clazz) {
        return TypeCheckerInjector.build(typeStoreBuilder.build(), context, references.build()).getInstance(clazz);
    }
    
    public StaticContext context() {
        return context;
    }
    
    public void addType(TypeDeclarationNode node, Type type) {
        typeStoreBuilder.add(node, type);
    }
    
    public void addReference(VariableIdentifierNode reference, Declaration declaration) {
        references.addReference(reference, declaration);
    }
    
    public VariableIdentifierNode stringTypeReference() {
        return STRING_TYPE_REFERENCE;
    }
    
    public Declaration stringTypeDeclaration() {
        return stringTypeDeclaration;
    }
    
    public VariableIdentifierNode unitTypeReference() {
        return UNIT_TYPE_REFERENCE;
    }
    
    public Declaration unitTypeDeclaration() {
        return unitTypeDeclaration;
    }
    
    public VariableIdentifierNode booleanTypeReference() {
        return BOOLEAN_TYPE_REFERENCE;
    }
    
    public Declaration booleanTypeDeclaration() {
        return booleanTypeDeclaration;
    }
    
    public VariableIdentifierNode doubleTypeReference() {
        return DOUBLE_TYPE_REFERENCE;
    }
    
    public Declaration doubleTypeDeclaration() {
        return doubleTypeDeclaration;
    }
    
    public InterfaceType interfaceType() {
        return interfaceType;
    }
    
    public ClassType implementingClassType() {
        return implementingClassType;
    }
    
    public VariableIdentifierNode implementingClassTypeReference() {
        return implementingClassTypeReference;
    }
    
    public Declaration implementingClassTypeDeclaration() {
        return implementingClassTypeDeclaration;
    }
    
    public VariableIdentifierNode interfaceTypeReference() {
        return interfaceTypeReference;
    }
    
    public Declaration interfaceTypeDeclaration() {
        return interfaceTypeDeclaration;
    }
    
}
