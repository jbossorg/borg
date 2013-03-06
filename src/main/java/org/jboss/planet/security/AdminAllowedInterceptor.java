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
import org.jboss.planet.model.SecurityUser;
import org.jboss.planet.service.SecurityService;

/**
 * Interceptor for CRUD operations
 * 
 * @author Libor Krzyzanek
 * @see SecurityService#hasPermissionCRUD(Object, CRUDOperationType)
 */
@AdminAllowed
@Interceptor
public class AdminAllowedInterceptor {

	@Inject
	private SecurityService securityService;

	@Inject
	private UserController userController;

	@AroundInvoke
	public Object checkAdmin(InvocationContext ctx) throws Exception {
		if (!userController.isLoggedIn()) {
			throw new UserNotLoggedInException();
		}

		SecurityUser user = securityService.getCurrentUser();
		if (!securityService.isAdmin(user)) {
			throw new PermissionException(user, " Only administrator is permitted for this action.");
		}
		return ctx.proceed();
	}

}
