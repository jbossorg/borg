/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.security;

/**
 * CRUD operation for securing business logic
 * 
 * @author Libor Krzyzanek
 * @see CRUDAllowed
 * @see org.jboss.planet.service.SecurityService
 */
public enum CRUDOperationType {

	/**
	 * All operations
	 */
	ALL,
	/**
	 * Create operation
	 */
	CREATE,
	/**
	 * Read operation
	 */
	READ,
	/**
	 * Update operation
	 */
	UPDATE,
	/**
	 * Delete operation
	 */
	DELETE
}
