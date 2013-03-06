/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.model;

import java.io.IOException;

/**
 * @author Adam Warski (adam at warski dot org)
 * @author Libor Krzyzanek
 */
public class CategoryRegexpFilter extends AbstractRegexpFilter {

	private static final long serialVersionUID = 1209854919382113132L;

	public CategoryRegexpFilter() {
	}

	public CategoryRegexpFilter(String regexp) {
		super(regexp);
	}

	public boolean filter(Post post) {
		for (Category cat : post.getCategories()) {
			if (pattern.matcher(cat.getName()).matches()) {
				return true;
			}
		}

		return false;
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();

		setRegexp(getRegexp());
	}

	public String toString() {
		return "Category matching regexp: " + regexp;
	}
}
