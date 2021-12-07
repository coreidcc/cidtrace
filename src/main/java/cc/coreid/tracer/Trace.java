package cc.coreid.tracer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
TODO: This is for now just a single simple annotation to illustrate that we can control the instrumentation.
      One should properly think how the different use cases can be supported. A preliminary list of these
      use cases are:
      a) class level
        a) include class in tracing which otherwise would be excluded (for now all classes are included)
        b) exclude class from tracing
      b) method level
        a) do instrument methods which otherwise would be excluded (e.g. non-public methods)
        b) exclude methods from being instrumented which otherwise would be instrumented (e.g. public methods)
      c) parameter level
        a) mask individual method parameters from being traced
        b) use a dedicated formatter for a given parameter

 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
public @interface Trace {
    boolean exclude() default false;
}
