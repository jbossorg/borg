/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.security;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.planet.controller.UserController;
import org.jboss.planet.service.SecurityService;

/**
 * Interceptor for CRUD operations
 * 
 * @author Libor Krzyzanek
 * @see SecurityService#hasPermission(org.jboss.planet.model.SecurityUser, Object, CRUDOperationType)
 */
@CRUDAllowed
@Interceptor
public class CRUDAllowedInterceptor {

	@Inject
	private SecurityService securityService;

	@Inject
	private UserController userController;

	@AroundInvoke
	public Object checkCRUD(InvocationContext ctx) throws Exception {
		if (!userController.isLoggedIn()) {
			throw new UserNotLoggedInException();
		}
		Object[] args = ctx.getParameters();
		CRUDAllowed allowed = getAnnotation(ctx.getMethod());

		if (args.length < 1) {
			throw new RuntimeException(
					"CRUD interceptor defined but method doesn't pass at least 1 parameter to be checked");
		}
		securityService.checkPermission(args[0], allowed.operation());

		return ctx.proceed();
	}

	private CRUDAllowed getAnnotation(Method m) {
		for (Annotation a : m.getAnnotations()) {
			if (a instanceof CRUDAllowed) {
				return (CRUDAllowed) a;
			}
		}

		throw new RuntimeException("@CRUDAllowed not found on method " + m.getName());
	}

}
