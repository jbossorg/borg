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

import org.hibernate.Session;
import org.hibernate.stat.Statistics;

/**
 * Service related to statistics
 * 
 * @author Libor Krzyzanek
 * 
 */
@Named
@Stateless
public class StatisticService {

	@Inject
	private EntityManager em;

	public Statistics getHibernateStatistics() {
		Session session = em.unwrap(Session.class);
		return session.getSessionFactory().getStatistics();
	}

}
