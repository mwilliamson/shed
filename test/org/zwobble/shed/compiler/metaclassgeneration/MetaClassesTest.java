package org.zwobble.shed.compiler.metaclassgeneration;

import org.junit.Test;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.ScalarType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.types.TypeMatchers.classTypeWithName;

public class MetaClassesTest {
    private final MetaClasses metaClasses = MetaClasses.create();
    
    @Test public void
    metaClassesAreGeneratedForEachType() {
        ClassType type = new ClassType(fullyQualifiedName("shed", "Song"));
        assertThat(metaClasses.metaClassOf(type), is(classTypeWithName(fullyQualifiedName("shed", "Song", "$Meta"))));
    }
    
    @Test public void
    typeCanBeRetrivedByMetaClass() {
        ScalarType type = new ClassType(fullyQualifiedName("shed", "Song"));
        ClassType metaClass = metaClasses.metaClassOf(type);
        assertThat(metaClasses.getTypeFromMetaClass(metaClass), is(type));
    }
    
    @Test public void
    canDetermineIfATypeIsAMetaClass() {
        ScalarType type = new ClassType(fullyQualifiedName("shed", "Song"));
        ClassType metaClass = metaClasses.metaClassOf(type);
        assertThat(metaClasses.isMetaClass(type), is(false));
        assertThat(metaClasses.isMetaClass(metaClass), is(true));
    }
}
