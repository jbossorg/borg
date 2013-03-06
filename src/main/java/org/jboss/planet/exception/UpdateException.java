/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.exception;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = false)
public class UpdateException extends Exception {

	private static final long serialVersionUID = -4311196904486765712L;

	public UpdateException() {
	}

	public UpdateException(String message) {
		super(message);
	}

	public UpdateException(String message, Throwable cause) {
		super(message, cause);
	}

	public UpdateException(Throwable cause) {
		super(cause);
	}
}
