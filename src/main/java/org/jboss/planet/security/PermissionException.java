/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.security;

import java.security.Principal;

/**
 * General permission exception
 * 
 * @author Libor Krzyzanek
 * @see AdminAllowed
 * @see AdminAllowedInterceptor
 */
public class PermissionException extends RuntimeException {

	private static final long serialVersionUID = 3682151881976011733L;

	public PermissionException(Principal user) {
		this(user, "");
	}

	public PermissionException(Principal user, String message) {
		super("No permissions for user " + user.getName() + ". " + message);
	}

	public PermissionException(Principal user, Throwable cause) {
		this(user, "", cause);
	}

	public PermissionException(Principal user, String message, Throwable cause) {
		super("No permissions for user " + user.getName() + ". " + message, cause);
	}

}
