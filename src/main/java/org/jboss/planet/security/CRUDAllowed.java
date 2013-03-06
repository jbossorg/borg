/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

/**
 * Annotation for securing business logic. <br/>
 * Can be used only on method which contains one argument acting as entity which will be checked. <br/>
 * If user is not permitted for particular operation then {@link PermissionCRUDException} is thrown
 * 
 * @param operation
 *            (optional) operation to check. Default is {@link CRUDOperationType#ALL};
 * 
 * @author Libor Krzyzanek
 * @see PermissionCRUDException
 * @see CRUDAllowedInterceptor
 */
@InterceptorBinding
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface CRUDAllowed {

	/**
	 * CRUD Operation. If not defined then CRUDOperationType.ALL is used
	 * 
	 * @return CRUD operation
	 */
	@Nonbinding
	CRUDOperationType operation() default CRUDOperationType.ALL;

}
