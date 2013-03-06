/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.model;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public enum XmlType {
	ATOM("application/atom+xml"), RSS2("application/xhtml+xml"), RSS1("application/xhtml+xml");

	private final String contentType;

	public String contentType() {
		return contentType;
	}

	XmlType(String contentType) {
		this.contentType = contentType;
	}
}
