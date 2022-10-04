package io.datacater.core;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({TYPE, METHOD, CONSTRUCTOR})
public @interface ExcludeFromGeneratedCoverageReport {}
