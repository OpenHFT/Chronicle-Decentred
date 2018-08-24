package town.lost.examples.exchange.util;

import java.lang.annotation.*;

@Documented
@Target(value = {ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface CouldBeNaN {

}
