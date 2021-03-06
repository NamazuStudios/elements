package com.namazustudios.socialengine.service;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Requests injection for the unscoped version of a particular service. Typically this means that the security checks
 * are bypassed.
 */
@Qualifier
@Retention(RUNTIME)
@Target({PARAMETER, FIELD})
public @interface Unscoped {}
