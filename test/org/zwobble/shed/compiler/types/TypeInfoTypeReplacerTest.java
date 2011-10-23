package org.zwobble.shed.compiler.types;

import org.junit.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.assignableValue;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
import static org.zwobble.shed.compiler.types.FormalTypeParameter.formalTypeParameter;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.Members.members;
import static org.zwobble.shed.compiler.types.ParameterisedType.parameterisedType;
import static org.zwobble.shed.compiler.types.TypeApplication.applyTypes;
import static org.zwobble.shed.compiler.types.Types.typeParameters;

public class TypeInfoTypeReplacerTest {
    @Test public void
    membersHaveTypeParametersUpdated() {
        FormalTypeParameter keyFormalTypeParameter = formalTypeParameter("K");
        FormalTypeParameter valueFormalTypeParameter = formalTypeParameter("V");
        InterfaceType dictionaryBaseType = new InterfaceType(fullyQualifiedName("Dictionary"));
        ScalarTypeInfo dictionaryTypeInfo = new ScalarTypeInfo(interfaces(), members(
            "get", unassignableValue(CoreTypes.functionTypeOf(keyFormalTypeParameter, valueFormalTypeParameter)),
            "targetLoad", assignableValue(CoreTypes.DOUBLE)
        ));
        ParameterisedType dictionaryType = parameterisedType(dictionaryBaseType, asList(keyFormalTypeParameter, valueFormalTypeParameter));
        TypeApplication concreteDictionaryType = applyTypes(dictionaryType, typeParameters(CoreTypes.STRING, CoreTypes.BOOLEAN));
        ScalarTypeInfo typeInfo = buildTypeInfo(concreteDictionaryType, dictionaryTypeInfo);
        assertThat(typeInfo.getMembers(), is(members(
            "get", unassignableValue(CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.BOOLEAN)),
            "targetLoad", assignableValue(CoreTypes.DOUBLE)
        )));
    }
    
    @Test public void
    interfacesHaveTypeParametersUpdated() {
        InterfaceType listBaseType = new InterfaceType(fullyQualifiedName("List"));
        ParameterisedType listType = parameterisedType(listBaseType, asList(formalTypeParameter("T")));
        
        ClassType arrayListBaseType = new ClassType(fullyQualifiedName("ArrayList"));
        FormalTypeParameter arrayListTypeParameter = formalTypeParameter("T");
        ParameterisedType arrayListType = parameterisedType(arrayListBaseType, asList(arrayListTypeParameter));
        ScalarTypeInfo arrayListTypeInfo = new ScalarTypeInfo(interfaces(applyTypes(listType, typeParameters(arrayListTypeParameter))), members());
        
        ScalarTypeInfo typeInfo = buildTypeInfo(applyTypes(arrayListType, typeParameters(CoreTypes.STRING)), arrayListTypeInfo);
        assertThat(typeInfo.getSuperTypes(), containsInAnyOrder((Type)applyTypes(listType, typeParameters(CoreTypes.STRING))));
    }
    
    private ScalarTypeInfo buildTypeInfo(TypeApplication typeApplication, ScalarTypeInfo typeInfo) {
        return new TypeInfoTypeReplacer(new TypeReplacer()).buildTypeInfo(typeApplication, typeInfo);
    }
}
