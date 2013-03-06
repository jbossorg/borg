/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.exception;

/**
 * Application Exception occurs in business logic occur data already exists
 * 
 * @author Libor Krzyzanek
 */
public class DuplicateEntryException extends Exception {

	private static final long serialVersionUID = 4987689345507212568L;

	public DuplicateEntryException() {
	}

	public DuplicateEntryException(String message) {
		super(message);
	}

	public DuplicateEntryException(String message, Throwable cause) {
		super(message, cause);
	}

	public DuplicateEntryException(Throwable cause) {
		super(cause);
	}
}
