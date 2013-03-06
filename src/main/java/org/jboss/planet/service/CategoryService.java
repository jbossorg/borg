/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.service;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.jboss.planet.model.Category;

@Stateless
@Named
public class CategoryService {

	@Inject
	private EntityManager em;

	public Category getCategory(String name) {
		try {
			return (Category) em.createQuery("select cat from Category cat where cat.name = ?1").setParameter(1, name)
					.getSingleResult();
		} catch (NoResultException e) {
			// TOOD: Think about more clear way how to create new category
			Category ret = new Category(name);
			em.persist(ret);
			em.flush();

			return ret;
		}
	}
}
