/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.security;

import java.security.Principal;

/**
 * User doesn't have permission to CRUD an entity
 * 
 * @author Libor Krzyzanek
 * @see CRUDAllowed
 * @see CRUDAllowedInterceptor
 * @see CRUDOperationType
 */
public class PermissionCRUDException extends PermissionException {

	private static final long serialVersionUID = -1255838771395126530L;

	public PermissionCRUDException(Principal user, Object entity, CRUDOperationType operation) {
		this(user, entity, operation, "");
	}

	public PermissionCRUDException(Principal user, Object entity, CRUDOperationType operation, String message) {
		super(user, "Operation " + operation + " on entity " + entity + ". " + message);
	}

	public PermissionCRUDException(Principal user, Object entity, CRUDOperationType operation, Throwable cause) {
		this(user, entity, operation, "", cause);
	}

	public PermissionCRUDException(Principal user, Object entity, CRUDOperationType operation, String message,
			Throwable cause) {
		super(user, "Operation " + operation + " on entity " + entity + ". " + message, cause);
	}

}
