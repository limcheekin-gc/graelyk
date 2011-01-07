package groovyx.gaelyk.graelyk.annotation

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
//GDC as in GraelykDomainClass
public @interface GDC {
	Class value();
}