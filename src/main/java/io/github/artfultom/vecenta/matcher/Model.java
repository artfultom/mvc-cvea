package io.github.artfultom.vecenta.matcher;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Model {

    String name();

    String[] order();

}
