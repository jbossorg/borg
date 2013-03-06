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

import org.jboss.planet.model.SecurityUser;

/**
 * Business logic related to {@link SecurityUser}
 * 
 * @author Libor Krzyzanek
 */
@Named
@Stateless
public class UserService extends EntityServiceJpa<SecurityUser> {

	@Inject
	private EntityManager em;

	public UserService() {
		super(SecurityUser.class);
	}

	/**
	 * Get user based on its externalId (username)
	 * 
	 * @param externalId
	 * @return
	 */
	public SecurityUser getUser(String externalId) {
		try {
			return (SecurityUser) em.createQuery("select user from SecurityUser user WHERE user.externalId = ?1")
					.setParameter(1, externalId).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	/**
	 * Create user. It doesn't check security permissions because it can be called when SecurityService cannot be
	 * established yet.
	 */
	@Override
	public SecurityUser create(SecurityUser t) {
		this.getEntityManager().persist(t);
		this.getEntityManager().flush();
		this.getEntityManager().refresh(t);

		created.fire(t);

		return t;
	}

}
