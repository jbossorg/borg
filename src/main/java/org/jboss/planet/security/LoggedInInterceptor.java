/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.security;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.planet.controller.UserController;

@LoggedIn
@Interceptor
public class LoggedInInterceptor {

	@Inject
	private UserController userController;

	@AroundInvoke
	public Object checkSecurity(InvocationContext ctx) throws Exception {
		if (!userController.isLoggedIn()) {
			throw new UserNotLoggedInException();
		}
		return ctx.proceed();
	}
}
