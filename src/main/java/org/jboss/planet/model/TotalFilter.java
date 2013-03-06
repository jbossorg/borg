/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.model;

public class TotalFilter implements PostFilter {

	private static final long serialVersionUID = -8839681871861904116L;

	public boolean filter(Post post) {
		return true;
	}

	public String toString() {
		return "Everything";
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof TotalFilter))
			return false;

		return true;
	}

	public int hashCode() {
		return TotalFilter.class.hashCode();
	}
}
