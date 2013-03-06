/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.security;

import org.jboss.planet.service.SecurityService;

/**
 * User is not logged in
 * 
 * @author Libor Krzyzanek
 * @see LoggedInInterceptor
 * @see SecurityService
 */
public class UserNotLoggedInException extends RuntimeException {

	private static final long serialVersionUID = 5525209702488098102L;

	public UserNotLoggedInException() {
	}

	public UserNotLoggedInException(String message) {
		super(message);
	}

	public UserNotLoggedInException(String message, Throwable cause) {
		super(message, cause);
	}

	public UserNotLoggedInException(Throwable cause) {
		super(cause);
	}
}
