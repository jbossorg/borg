/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.exception;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = false)
public class ParserException extends Exception {

	private static final long serialVersionUID = -5144378591963600455L;

	public static enum CAUSE_TYPE {
		/** Problem during connecting to feed */
		CONNECTION_PROBLEM,
		/** Problem during parsing a feed */
		PARSING_EXCEPTION
	};

	private CAUSE_TYPE causeType;

	public ParserException() {
	}

	public ParserException(String message, CAUSE_TYPE causeType) {
		super(message);
		this.causeType = causeType;
	}

	public ParserException(String message, Throwable cause, CAUSE_TYPE causeType) {
		super(message, cause);
		this.causeType = causeType;
	}

	public ParserException(Throwable cause, CAUSE_TYPE causeType) {
		super(cause);
		this.causeType = causeType;
	}

	public CAUSE_TYPE getCauseType() {
		return causeType;
	}

	public void setCauseType(CAUSE_TYPE causeType) {
		this.causeType = causeType;
	}
}
