package groovyx.gaelyk.graelyk.annotation

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
//@Contains(GenericType) is used to specify the generic type of a Key or Collection
//since generics don't seem to be available for reflection in GAE
public @interface Contains {
	Class value();
}