/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.model;

import java.util.regex.Pattern;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public abstract class AbstractRegexpFilter implements PostFilter {

	private static final long serialVersionUID = -1424014059345340894L;

	// TODO: Check if pattern regexp works
	@javax.validation.constraints.Pattern(regexp = "*")
	protected String regexp;

	protected transient Pattern pattern;

	protected AbstractRegexpFilter() {
	}

	protected AbstractRegexpFilter(String regexp) {
		setRegexp(regexp);
	}

	public String getRegexp() {
		return regexp;
	}

	public void setRegexp(String regexp) {
		this.regexp = regexp;

		pattern = Pattern.compile(this.regexp);
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof CategoryRegexpFilter))
			return false;

		AbstractRegexpFilter that = (AbstractRegexpFilter) o;

		if (regexp != null ? !regexp.equals(that.regexp) : that.regexp != null)
			return false;

		return true;
	}

	public int hashCode() {
		return (regexp != null ? regexp.hashCode() : 0);
	}
}
