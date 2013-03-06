/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.util;

/**
 * Custom exception handler
 * 
 * @author Libor Krzyzanek
 */
public class ExceptionHandlerFactory extends javax.faces.context.ExceptionHandlerFactory {

	private final javax.faces.context.ExceptionHandlerFactory parent;

	public ExceptionHandlerFactory(final javax.faces.context.ExceptionHandlerFactory parent) {
		this.parent = parent;
	}

	@Override
	public ExceptionHandler getExceptionHandler() {
		return new ExceptionHandler(this.parent.getExceptionHandler());
	}

}
